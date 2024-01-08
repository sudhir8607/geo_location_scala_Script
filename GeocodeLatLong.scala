import io.gatling.core.Predef._
import io.gatling.http.Predef._
import scala.concurrent.duration._

class GeocodeLatLong extends Simulation {

  // Load all data from the CSV file
  val dataFeeder = csv("./user-files/data/Latitude Longitude Test Data.csv").random

  // Define the HTTP protocol without query parameters
  val httpProtocol = http
    .baseUrl("https://geopoc.stagingapps.xyz")
    .acceptHeader("application/json")

  // Define the scenario
  val scn = scenario("Get Address Scenario")
    .feed(dataFeeder)
    .exec(http("Get Address Request")
      .post("/getAddress")
      .headers(Map("Content-Type" -> "application/json", "Authorization" -> "Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJlbWFpbCI6InVzZXJpZEBlbWFpbC5jb20iLCJwYXNzd29yZCI6InRlc3RAMTIzIiwiaWF0IjoxNzA0NTM1OTMwLCJleHAiOjE3MDgxMzU5MzB9.-WJBtZdM2kbY7Knv_zoif45DMXLHEQhS6wb1-OQcXJs"))
      .body(StringBody(
        s"""
           |{
           |  "latitude": "#{latitude}",
           |  "longitude": "#{longitude}"
           |}
        """.stripMargin))
      .check(status.is(200))
      .check(jsonPath("$.data.address").saveAs("locationDetails"))
    )
    .exec(session => {
      val latitude = session("latitude").as[String]
      val longitude = session("longitude").as[String]
      val locationDetails = session("locationDetails").as[String]
      println(s"Latitude: $latitude, Longitude: $longitude, Location: $locationDetails")
      session
    })

  // Define the load simulation
  setUp(
    scn.inject(
      rampUsersPerSec(1) to 50 during (10 seconds),
      constantUsersPerSec(50) during (60 seconds)
    )
  ).protocols(httpProtocol)
}