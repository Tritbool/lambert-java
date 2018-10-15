package net.yageek.lambert

import scala.math._
import net.yageek.lambert.ZoneCode._
import net.yageek.lambert.Constants._
import sun.reflect.generics.reflectiveObjects.NotImplementedException

/*
https://github.com/yageek/lambert-java
https://bintray.com/yageek/maven/lambert-java/view/files/net/yageek/lambert/lambert-java/1.1

Online samples :
http://geofree.fr/gf/coordinateConv.asp#listSys

--------------------------------------------------------------------------------------
Install cs2cs on Ubuntu :
http://www.sarasafavi.com/installing-gdalogr-on-ubuntu.html

--------------------------------------------------------------------------------------
http://cs2cs.mygeodata.eu/
Conversion From Lambert Zone II to WGS 84 :
$>cs2cs +proj=lcc +lat_1=46.8 +lat_0=46.8 +lon_0=0 +k_0=0.99987742 +x_0=600000 +y_0=2200000 +a=6378249.2 +b=6356515 +towgs84=-168,-60,320,0,0,0,0 +pm=paris +units=m +no_defs +to +proj=longlat +datum=WGS84 +no_defs -f "%.11f" <<EOF
> 618115 2430676
> EOF

2.58331732871	48.87414278182 43.05512374267

--------------------------------------------------------------------------------------
Conversion From WGS 84 To Lambert Zone II:
$>cs2cs +proj=longlat +datum=WGS84 +no_defs +to +proj=lcc +lat_1=46.8 +lat_0=46.8 +lon_0=0 +k_0=0.99987742 +x_0=600000 +y_0=2200000 +a=6378249.2 +b=6356515 +towgs84=-168,-60,320,0,0,0,0 +pm=paris +units=m +no_defs  -f "%.11f" <<EOF
2.58331732871 48.8741427818
EOF
618115.00035284588	2430676.00004872493 -43.05512374081
*/


/*
Documentations :
http://geodesie.ign.fr/contenu/fichiers/documentation/algorithmes/notice/NTG_71.pdf
http://geodesie.ign.fr/contenu/fichiers/documentation/algorithmes/notice/NTG_80.pdf
http://geodesie.ign.fr/contenu/fichiers/documentation/pedagogiques/TransformationsCoordonneesGeodesiques.pdf
 */

object Lambert {

  /*
  *   ALGO0001
  */
  def latitudeISOFromLat(lat: Double, e: Double): Double = {
    val elt11: Double = Pi / 4d
    val elt12: Double = lat / 2d
    val elt1: Double = tan(elt11 + elt12)
    val elt21: Double = e * sin(lat)
    val elt2: Double = pow((1 - elt21) / (1 + elt21), e / 2d)

    log(elt1 * elt2)
  }


  /*
  *   ALGO0002
  */
  def latitudeFromLatitudeISO(latISo: Double, e: Double, eps: Double): Double = {

    var phi0: Double = 2 * atan(exp(latISo)) - M_PI_2
    var phiI: Double = 2 * atan(pow((1 + e * sin(phi0)) / (1 - e * sin(phi0)), e / 2d) * exp(latISo)) - M_PI_2
    var delta: Double = abs(phiI - phi0)

    while (delta > eps) {
      phi0 = phiI
      phiI = 2 * atan(pow((1 + e * sin(phi0)) / (1 - e * sin(phi0)), e / 2d) * exp(latISo)) - M_PI_2
      delta = abs(phiI - phi0)
    }

    phiI
  }


  /*
 *   ALGO0003
 */
  def geographicToLambertAlg003(latitude: Double, longitude: Double, zone: LambertZone, lonMeridian: Double, e: Double): LambertPoint = {

    val n: Double = zone.n()
    val C: Double = zone.c()
    val xs: Double = zone.xs()
    val ys: Double = zone.ys()

    val latIso: Double = latitudeISOFromLat(latitude, e)

    val eLatIso: Double = exp(-n * latIso)

    val nLon: Double = n * (longitude - lonMeridian)

    val x: Double = xs + C * eLatIso * sin(nLon)
    val y: Double = ys - C * eLatIso * cos(nLon)

    new LambertPoint(x, y, 0)
  }

  /*
 *  http://geodesie.ign.fr/contenu/fichiers/documentation/pedagogiques/TransformationsCoordonneesGeodesiques.pdf
 *  3.4 Coordonnées géographiques Lambert
 */
  def geographicToLambert(latitude: Double, longitude: Double, zone: LambertZone, lonMeridian: Double, e: Double): LambertPoint = {

    val n: Double = zone.n()
    val C: Double = zone.c()
    val xs: Double = zone.xs()
    val ys: Double = zone.ys()

    val sinLat: Double = sin(latitude)
    val eSinLat: Double = e * sinLat
    val elt1: Double = (1 + sinLat) / (1 - sinLat)
    val elt2: Double = (1 + eSinLat) / (1 - eSinLat)

    val latIso: Double = (1 / 2d) * log(elt1) - (e / 2d) * log(elt2)

    val R: Double = C * exp(-(n * latIso))

    val LAMBDA: Double = n * (longitude - lonMeridian)

    val x: Double = xs + (R * sin(LAMBDA))
    val y: Double = ys - (R * cos(LAMBDA))

    new LambertPoint(x, y, 0)
  }

  /*
  *	ALGO0004 - Lambert vers geographiques
  */

  def lambertToGeographic(org: LambertPoint, zone: LambertZone, lonMeridian: Double, e: Double, eps: Double): LambertPoint = {
    val n: Double = zone.n()
    val C: Double = zone.c()
    val xs: Double = zone.xs()
    val ys: Double = zone.ys()

    val x: Double = org.getX()
    val y: Double = org.getY()

    val R: Double = sqrt((x - xs) * (x - xs) + (y - ys) * (y - ys))
    val gamma: Double = atan((x - xs) / (ys - y))
    val lon: Double = lonMeridian + gamma / n
    val latIso: Double = -1 / n * log(abs(R / C))
    val lat: Double = latitudeFromLatitudeISO(latIso, e, eps)

    new LambertPoint(lon, lat, 0)
  }

  /*
  * ALGO0021 - Calcul de la grande Normale
  *
 */

  private def lambertNormal(lat: Double, a: Double, e: Double): Double = {

    a / sqrt(1 - e * e * sin(lat) * sin(lat))
  }

  /*
   * ALGO0009 - Transformations geographiques -> cartésiennes
   *
   */

  private def geographicToCartesian(lon: Double, lat: Double, he: Double, a: Double, e: Double): LambertPoint = {
    val N: Double = lambertNormal(lat, a, e)

    val pt: LambertPoint = new LambertPoint(0, 0, 0)

    pt.setX((N + he) * cos(lat) * cos(lon))
    pt.setY((N + he) * cos(lat) * sin(lon))
    pt.setZ((N * (1 - e * e) + he) * sin(lat))

    pt

  }

  /*
* ALGO0012 - Passage des coordonnées cartésiennes aux coordonnées géographiques
*/

  private def cartesianToGeographic(org: LambertPoint, meridien: Double, a: Double, e: Double, eps: Double): LambertPoint = {
    val x: Double = org.getX()
    val y: Double = org.getY()
    val z: Double = org.getZ()

    val lon: Double = meridien + atan(y / x)

    val module: Double = sqrt(x * x + y * y)

    var phi0: Double = atan(z / (module * (1 - (a * e * e) / sqrt(x * x + y * y + z * z))))
    var phiI: Double = atan(z / module / (1 - a * e * e * cos(phi0) / (module * sqrt(1 - e * e * sin(phi0) * sin(phi0)))))
    var delta: Double = abs(phiI - phi0)
    while (delta > eps) {
      phi0 = phiI
      phiI = atan(z / module / (1 - a * e * e * cos(phi0) / (module * sqrt(1 - e * e * sin(phi0) * sin(phi0)))))
      delta = abs(phiI - phi0)

    }

    val he: Double = module / cos(phiI) - a / sqrt(1 - e * e * sin(phiI) * sin(phiI))

    new LambertPoint(lon, phiI, he)
  }

  /*
* Convert Lambert -> WGS84
* http://geodesie.ign.fr/contenu/fichiers/documentation/pedagogiques/transfo.pdf
*
*/

  def convertToWGS84(org: LambertPoint, zone: LambertZone): LambertPoint = {

    var lp: Option[LambertPoint] = Option.empty

    if (zone.zone == Lambert93) {
      lp = Option(lambertToGeographic(org,zone, LON_MERID_IERS, E_WGS84, DEFAULT_EPS))
    } else {
      val pt1: LambertPoint = lambertToGeographic(org, zone, LON_MERID_PARIS, E_CLARK_IGN, DEFAULT_EPS)

      val pt2: LambertPoint = geographicToCartesian(pt1.getX(), pt1.getY(), pt1.getZ(), A_CLARK_IGN, E_CLARK_IGN)

      pt2.translate(-168, -60, 320)

      //WGS84 refers to greenwich
      lp = Option(cartesianToGeographic(pt2, LON_MERID_GREENWICH, A_WGS84, E_WGS84, DEFAULT_EPS))
    }
    lp.orNull
  }

  /*
* Convert WGS84 -> Lambert
* http://geodesie.ign.fr/contenu/fichiers/documentation/pedagogiques/transfo.pdf
*
*/

  def convertToLambert(latitude: Double, longitude: Double, zone: LambertZone): LambertPoint = {
    var lp: Option[LambertPoint] = Option.empty
    if (zone.zone == Lambert93) {
      throw new NotImplementedException()
    } else {
      val pt1: LambertPoint = geographicToCartesian(longitude - LON_MERID_GREENWICH, latitude, 0, A_WGS84, E_WGS84)

      pt1.translate(168, 60, -320)

      val pt2: LambertPoint = cartesianToGeographic(pt1, LON_MERID_PARIS, A_WGS84, E_WGS84, DEFAULT_EPS)

      lp = Option(geographicToLambert(pt2.getY(), pt2.getX(), zone, LON_MERID_PARIS, E_WGS84))
    }
    lp.orNull
  }

  /*
      Method not really usefull, just to have two ways of doing the same conversion.
   */
  def convertToLambertByAlg003(latitude: Double, longitude: Double, zone: LambertZone): LambertPoint = {
    var lp: Option[LambertPoint] = Option.empty
    if (zone.zone == Lambert93) {
      throw new NotImplementedException()
    } else {
      val pt1: LambertPoint = geographicToCartesian(longitude - LON_MERID_GREENWICH, latitude, 0, A_WGS84, E_WGS84)

      pt1.translate(168, 60, -320);

      val pt2: LambertPoint = cartesianToGeographic(pt1, LON_MERID_PARIS, A_WGS84, E_WGS84, DEFAULT_EPS)

      lp = Option(geographicToLambertAlg003(pt2.getY(), pt2.getX(), zone, LON_MERID_PARIS, E_WGS84))
    }
    lp.orNull
  }

  def convertToWGS84(x: Double, y: Double, zone: LambertZone): LambertPoint = {

    val pt: LambertPoint = new LambertPoint(x, y, 0)
    convertToWGS84(pt, zone)
  }

  def convertToWGS84Deg(x: Double, y: Double, zone: LambertZone): LambertPoint = {

    val pt: LambertPoint = new LambertPoint(x, y, 0)
    convertToWGS84(pt, zone).toDegree()
  }

}


