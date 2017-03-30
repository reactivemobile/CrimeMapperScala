/**
  * Created by donalocallaghan on 29/03/2017.
  */

import scalaj.http._
import net.liftweb.json._

object Main {

  val BASE_URL = "http://data.police.uk/api/"
  val AVAILABILITY_URL: String = BASE_URL + "crimes-street-dates"
  val CATEGORY_URL: String = BASE_URL + "crime-categories"
  val STREET_CRIME_REPORT_URL: String = BASE_URL + "crimes-street/all-crime"
  val TIMEOUT: Int = 5000000
  val COLUMN_WIDTH: Int = 60

  var categoryMap: Map[String, String] = Map()
  var availabilityList: List[String] = List()
  var dateSelected: String = ""
  var availabilityOnly: Boolean = false

  // Default location is central London
  var latitude: String = "51.507983"
  var longitude: String = "-0.127545"

  implicit val formats = net.liftweb.json.DefaultFormats

  def loadCrimes(): Unit = {
    println(
      "Getting crime reports for " + dateSelected + " for location (" + latitude + ", " + longitude + ")")

    val response: HttpResponse[String] = Http(STREET_CRIME_REPORT_URL)
      .param("lat", latitude)
      .timeout(TIMEOUT, TIMEOUT)
      .param("lng", longitude)
      .param("date", dateSelected)
      .asString

    val responseJson = parse(response.body).children

    println("-" * (COLUMN_WIDTH * 3))
    addPaddingAndPrint(Array("Category", "Location", "Outcome"))
    println("-" * (COLUMN_WIDTH * 3))

    for (item <- responseJson) {
      val category = item \ "category"
      val categoryKey = if (category != null) category.extract[String] else "none"
      val categoryString = if (categoryMap.contains(categoryKey)) categoryMap(categoryKey) else "no category available"

      val locationString = (item \ "location" \ "street" \ "name").extract[String]

      val outcome = item \ "outcome_status"
      val outcomeString = if (outcome != JNull) (outcome \ "date").extract[String] + " " + (outcome \ "category").extract[String] else "No outcome available"

      addPaddingAndPrint(Array(categoryString, locationString, outcomeString))
    }
  }

  def addPaddingAndPrint(strings: Array[String]): Unit = {
    var toPrint: StringBuilder = StringBuilder.newBuilder
    for (string <- strings) {
      toPrint.append(string.padTo(COLUMN_WIDTH, ' '))
    }
    println(toPrint)
  }

  def loadCategoryMap(): Unit = {
    println("Getting crime report categories")
    val response: HttpResponse[String] = Http(CATEGORY_URL).asString
    val responseJson = parse(response.body).children

    for (item <- responseJson) {
      val urlString = (item \ "url").extract[String]
      val nameString = (item \ "name").extract[String]

      categoryMap += (urlString -> nameString)
    }

    loadCrimes()
  }

  def loadAvailability(): Unit = {
    println("Getting availability")
    val response: HttpResponse[String] = Http(AVAILABILITY_URL).asString

    val responseJson = parse(response.body).children

    if (availabilityOnly) {
      println("Available dates:")
    }

    for (item <- responseJson) {
      var itemString = (item \ "date").extract[String]
      availabilityList :+= itemString
      if (availabilityOnly) {
        println(itemString)
      }
    }

    if (!availabilityOnly) {
      dateSelected = availabilityList.head
      loadCategoryMap()
    }
  }

  def main(args: Array[String]): Unit = {
    if (args.contains("-location")) {
      val locationStrings = args(args.indexOf("-location") + 1).split(",")
      latitude = locationStrings(0)
      longitude = locationStrings(1)
    }

    if (args.contains("-availability")) {
      availabilityOnly = true
      loadAvailability()
    }
    else if (args.contains("-date")) {
      dateSelected = args(args.indexOf("-date") + 1)
      loadCategoryMap()
    }
    else {
      // Default
      loadAvailability()
    }
  }
}
