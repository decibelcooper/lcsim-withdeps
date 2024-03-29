/*
 * HitCylPhiGenerator_Test.java
 *
 * Created on July 24, 2007, 8:44 PM
 *
 * $Id: HitCylPhiGenerator_Test.java,v 1.1.1.1 2010/04/08 20:38:00 jeremy Exp $
 */

package org.lcsim.recon.tracking.trfcyl;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import junit.framework.TestCase;
import org.lcsim.recon.tracking.trfbase.BoundedStat;
import org.lcsim.recon.tracking.trfbase.Cluster;
import org.lcsim.recon.tracking.trfbase.CrossStat;
import org.lcsim.recon.tracking.trfbase.ETrack;
import org.lcsim.recon.tracking.trfbase.Hit;
import org.lcsim.recon.tracking.trfbase.Surface;
import org.lcsim.recon.tracking.trfbase.TrackError;
import org.lcsim.recon.tracking.trfbase.TrackVector;
import org.lcsim.recon.tracking.trfbase.VTrack;
import org.lcsim.recon.tracking.trfutil.Assert;
import org.lcsim.recon.tracking.trfutil.TRFMath;

/**
 *
 * @author Norman Graf
 */
public class HitCylPhiGenerator_Test extends TestCase
{
	private boolean debug;
	/** Creates a new instance of HitCylPhiGenerator_Test */
	public void testHitCylPhiGenerator()
	{
		String component = "HitCylPhiGenerator";
		String ok_prefix = component + " (I): ";
		String error_prefix = component + " test (E): ";

		if(debug) System.out.println( ok_prefix
				+ "---------- Testing component " + component
				+ ". ----------" );

		//********************************************************************
		if(debug) System.out.println( ok_prefix + "Test constructor." );
		double radius = 25.0;
		double dphi = 0.01;
		TestSurfCylinder tcy = new TestSurfCylinder(radius);
		if(debug) System.out.println(tcy);
		HitCylPhiGenerator gen = new HitCylPhiGenerator(tcy,dphi);
		if(debug) System.out.println( gen );

		//********************************************************************

		if(debug) System.out.println( ok_prefix + "Generate a list of hits." );
		double phi0 = 1.2345;
		Surface srf = new SurfCylinder(radius);
		TrackVector vec = new TrackVector();
		vec.set(SurfCylinder.IPHI, phi0);
		TrackError err = new TrackError();
		VTrack trv = new VTrack(srf,vec);
		ETrack tre = new ETrack(trv,err);
		List clusters = new ArrayList();
		int nclus = 20;
		for ( int i=0; i<nclus; ++i )
		{
			Cluster clu = gen.newCluster(trv);
			Assert.assertTrue( clu != null );
			clusters.add( clu );
		}
		// Verify and display the list.
		List hits = new ArrayList();
		for ( Iterator iclu=clusters.iterator(); iclu.hasNext(); )
		{
			Cluster clu = (Cluster) iclu.next();
			Assert.assertTrue( clu != null );
			List newhits = clu.predict(tre,clu);
			Assert.assertTrue( newhits.size() == 1 );
			hits.add( newhits.get(0) );
		}
		Assert.assertTrue( hits.size() == nclus );

		//********************************************************************

		if(debug) System.out.println( ok_prefix + "Generate list of measured values." );
		double avg = 0.0;
		double sdev = 0.0;
		for ( Iterator ihit=hits.iterator(); ihit.hasNext(); )
		{
			Hit hit = (Hit) ihit.next();
			Assert.assertTrue( hit != null );
			double mval = hit.measuredVector().get(0);
			double merr = hit.measuredError().get(0,0);
			double pval = hit.predictedVector().get(0);
			double perr = hit.predictedError().get(0,0);
			Assert.assertTrue( TRFMath.isEqual(pval,phi0) );
			Assert.assertTrue( TRFMath.isEqual(merr,dphi*dphi) );
			if(debug) System.out.println( mval );
			avg += mval;
			sdev += (mval-pval)*(mval-pval);
		}
		avg /= nclus;
		sdev = Math.sqrt(sdev/nclus);
		if(debug) System.out.println( " Avg: " + avg );
		if(debug) System.out.println( "Sdev: " + sdev );
		Assert.assertTrue( Math.abs( avg - phi0 ) < dphi );
		Assert.assertTrue( Math.abs( sdev - dphi ) < dphi );

		//********************************************************************

		if(debug) System.out.println( ok_prefix + "Generate a hit out-of-bounds." );
		vec.set(SurfCylinder.IZ, 200.0);
		trv.setVector(vec);
		Assert.assertTrue( gen.newCluster(trv) == null );

		//********************************************************************

		if(debug) System.out.println( ok_prefix + "Print final generator state." );
		if(debug) System.out.println( gen );

		//********************************************************************

		if(debug) System.out.println( ok_prefix
				+ "------------- All tests passed. -------------" );

		//********************************************************************

	}

	//  SurfCylinder subclass for testing.
	//  z > 100 is considered out of bounds.
	static class TestSurfCylinder extends SurfCylinder
	{
		// static methods
		// Return the type name.
		public static String typeName()
		{ return "TestSurfCylinder"; }
		// Return the type.
		public static String staticType()
		{ return typeName(); }
		public double _zmax;


		public TestSurfCylinder(double radius)
		{
			super(radius);
		}

		public CrossStat status( ETrack tre)
		{
			return status( (VTrack) tre );
		}

		// Return the type.
		public String type()
		{ return staticType(); }

		public CrossStat status( VTrack trv )
		{
			CrossStat xstat = pureStatus(trv);
			if ( ! xstat.at() ) return xstat;
			double z = trv.vector(SurfCylinder.IZ);
			if ( z > 100.0 ) return new CrossStat(BoundedStat.OUT_OF_BOUNDS);
			return new CrossStat(BoundedStat.IN_BOUNDS);
		}
		public Surface newSurface()
		{
			return new TestSurfCylinder(radius());
		}

		public String toString()
		{
			return "TestSurfCylinder with radius "+_radius;
		}
	}

}

