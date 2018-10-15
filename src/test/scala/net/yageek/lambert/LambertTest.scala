package net.yageek.lambert

;

import junit.framework.Assert.assertNotNull
import net.yageek.lambert.ZoneCode._
import net.yageek.lambert.Constants._
import org.junit.Assert.assertEquals
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import java.io.ByteArrayOutputStream
import java.io.PrintStream

import com.typesafe.scalalogging.LazyLogging

//@RunWith(JUnit4.class)
class LambertTest extends LazyLogging {
  // private val outContent: ByteArrayOutputStream = new ByteArrayOutputStream()
  // private val errContent: ByteArrayOutputStream = new ByteArrayOutputStream()

//  @Before
//  def setUpStreams() = {
//      System.setOut(new PrintStream(outContent))
//     System.setErr(new PrintStream(errContent))
//  }
//
//  @After
//  def cleanUpStreams() = {
//       System.setOut(null)
//      System.setErr(null)
//  }

  @Test
  def ResultTest() = {
    val pt: LambertPoint = Lambert.convertToWGS84Deg(994272.661, 113467.422, new LambertZone(LambertI))
    logger.info("Point latitude:" + pt.getY() + " longitude:" + pt.getX())
  }

  @Test
  def Lambert93BugTest() = {
    val pt: LambertPoint = Lambert.convertToWGS84Deg(668832.5384, 6950138.7285, new LambertZone(Lambert93))
    assertEquals(2.56865, pt.getX(), 0.0001);
    assertEquals(49.64961, pt.getY(), 0.0001);
  }

  @Test
  def LambertIIExtendedToWgs84Test() = {
    val pt: LambertPoint = Lambert.convertToWGS84Deg(618115, 2430676, new LambertZone(LambertIIExtended))
    assertEquals(2.58331732871, pt.getX(), 0.0001) // Longitude 2.5832231178521186
    assertEquals(48.8741427818, pt.getY(), 0.0001) // Latitude 48.87412734248018
  }

    @Test
    def LambertAlg0001Test() ={
      val lat:Double = Lambert.latitudeISOFromLat(0.87266462600, 0.08199188998)
      assertNotNull(lat)
      assertEquals(1.00552653649, lat, 0.0001)

      val lat2:Double = Lambert.latitudeISOFromLat(-0.30000000000, 0.08199188998)
      assertNotNull(lat2)
      assertEquals(-0.30261690063, lat2, 0.0001)
    }

    /*
    Avec les données de tests pour valider l'algorythme
    http://geodesie.ign.fr/contenu/fichiers/documentation/algorithmes/notice/NTG_71.pdf
    ALG0003
     */
    @Test
    def LambertAlg0003Test()= {
      val latitude:Double = 0.87266462600
      val longitude:Double = 0.14551209900

      val lambertPoint:LambertPoint = Lambert.geographicToLambertAlg003(latitude, longitude, new LambertZone(LambertI), LON_MERID_GREENWICH, E_CLARK_IGN)

      assertEquals(1029705.0818, lambertPoint.getX(), 0.0001)
      assertEquals(272723.84730, lambertPoint.getY(), 0.0001)
    }

    /*
    Calcul par Algorythme ALG003
     */
    @Test
    def ConvertWGS84ToLambertByAlg0003Test() ={
      val latitude:Double = 48.87412734248018 // 48.8741427818;
      val latitude2:Double = latitude * 400 / 360 // Grad
      val radLat:Double = Math.toRadians(latitude)
      val longitude:Double = 2.5832231178521186 //2.58331732871;
      val longitude2:Double = longitude * 400 / 360 // Grad
      val radLong:Double = Math.toRadians(longitude)

      val lambertPoint:LambertPoint = Lambert.convertToLambertByAlg003(radLat, radLong, new LambertZone(LambertIIExtended))

      assertEquals(618115, lambertPoint.getX(), 1)
      assertEquals(2430676, lambertPoint.getY(), 1)
    }


    /*
        Methode provenant de
        http://geodesie.ign.fr/contenu/fichiers/documentation/pedagogiques/TransformationsCoordonneesGeodesiques.pdf
        3.4 attaquée en direct.
        avec les valeurs calculées précedemment sans la translation +towgs84=-168,-60,320,0,0,0,0
    */
    @Test
    def LambertGeographicToLambertTest() ={
      val latitude:Double = 48.8741427818
      val radLat:Double = Math.toRadians(latitude)
      val longitude:Double = 2.58331732871
      val radLong:Double = Math.toRadians(longitude)

      val lambertPoint:LambertPoint = Lambert.geographicToLambert(radLat, radLong, new LambertZone(LambertIIExtended), LON_MERID_GREENWICH, E_CLARK_IGN)

      assertEquals(618062, lambertPoint.getX(), 1)
      assertEquals(2430668, lambertPoint.getY(), 1)
    }

    /*
        Methode provenant de
        http://geodesie.ign.fr/contenu/fichiers/documentation/pedagogiques/TransformationsCoordonneesGeodesiques.pdf
        3.4 attaquée en direct
        avec les données de test
    */
    @Test
    def LambertConvertNTFToLambertTest() ={
      val latitude:Double = 51.8072313 // Grad
      val radLat:Double = Math.toRadians(latitude * 360d / 400d) // Deg before Rad
      val longitude:Double = 0.4721669 //Grad
      val radLong:Double = Math.toRadians(longitude * 360d / 400d) // Deg before Rad

      val lambertPoint:LambertPoint = Lambert.geographicToLambert(radLat, radLong, new LambertZone(LambertII), LON_MERID_PARIS, E_CLARK_IGN)

      assertEquals(632542.058, lambertPoint.getX(), 0.001)
      assertEquals(180804.145, lambertPoint.getY(), 0.01)
    }

    /*
        Validation de la méthode
        http://geodesie.ign.fr/contenu/fichiers/documentation/pedagogiques/TransformationsCoordonneesGeodesiques.pdf
        3.3 attaquée en direct
     */
    @Test
    def LamberConvertLambertToNTFTest() ={
      val X:Double = 1029705.083
      val Y:Double = 272723.849

      val lambertPoint:LambertPoint = Lambert.lambertToGeographic(new LambertPoint(X, Y, 0), new LambertZone(LambertI), LON_MERID_PARIS, E_CLARK_IGN, DEFAULT_EPS)

      assertEquals(0.145512099, lambertPoint.getX(), 10) // Longitude en rad
      assertEquals(0.872664626, lambertPoint.getY(), 10) // Latitude en trad
    }

    /*
        Test avec translation avant conversion
        .translate(168,60,-320);
    */
    @Test
    def LambertConvertToLambertTest() ={
      //double latitude = 48.8741427818;
      val latitude:Double = 48.87412734248018 // 48.8741427818;
      val radLat = Math.toRadians(latitude)
      //double longitude = 2.58331732871;
      val longitude:Double = 2.5832231178521186 //2.58331732871;
      val radLong:Double = Math.toRadians(longitude)

      val lambertPoint:LambertPoint = Lambert.convertToLambert(radLat, radLong, new LambertZone(LambertIIExtended))

      assertEquals(618115, lambertPoint.getX(), 1)
      assertEquals(2430676, lambertPoint.getY(), 1)
    }

}
