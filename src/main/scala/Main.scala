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

    println("Category \t\t Location \t\t ID \t\t Outcome")

    val tab: String = "\t\t"

    for (item <- responseJson) {
      val category = item \ "category"
      val categoryKey = if (category != null) category.extract[String] else "none"
      val categoryString = if (categoryMap.contains(categoryKey)) categoryMap(categoryKey) else "no category available"
      val locationString = (item \ "location" \ "street" \ "name").extract[String]

      val persistent_id = (item \ "persistent_id").extract[String]
      val persistentIdClean = if (persistent_id.length == 0) "no id available" else persistent_id

      val outcome = item \ "outcome_status"
      val outcomeString = if (outcome != JNull) (outcome \ "category").extract[String] else "No outcome available"

      println(categoryString + tab + locationString + tab + persistentIdClean + tab + outcomeString)
    }
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

    if(availabilityOnly) {
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
    for (arg <- args)
      println("Arg "+arg)

    if(args.contains("-location"))
      {
        val locationStrings = args(args.indexOf("-location")+1).split(",")
        latitude = locationStrings(0)
        longitude = locationStrings(1)
      }

    if(args.contains("-availability"))
      {
        availabilityOnly = true
        loadAvailability()
      }
    else
    if(args.contains("-date"))
      {
        dateSelected = args(args.indexOf("-date")+1)
        loadCategoryMap()
      }
    else
      {
        // Default
        loadAvailability()
      }
  }
}
