import com.github.polomarcus.utils.ClimateService
import com.github.polomarcus.model.CO2Record
import org.scalatest.funsuite.AnyFunSuite

//@See https://www.scalatest.org/scaladoc/3.1.2/org/scalatest/funsuite/AnyFunSuite.html
class ClimateServiceTest extends AnyFunSuite {
  test("containsWordGlobalWarming - non climate related words should return false") {
    assert(ClimateService.isClimateRelated("pizza") == false)
  }

  test("isClimateRelated - climate related words should return true") {
    assert(ClimateService.isClimateRelated("climate change") == true)
    assert(ClimateService.isClimateRelated("IPCC"))
  }

  //@TODO
  test("parseRawData") {
    // our inputs
    val firstRecord = (2003, 1, 355.2) //help: to acces 2003 of this tuple, you can do firstRecord._1
    val secondRecord = (2004, 1, 375.2)
    val list1 = List(firstRecord, secondRecord)

    // our output of our method "parseRawData"
    val co2RecordWithType = CO2Record(firstRecord._1, firstRecord._2, firstRecord._3)
    val co2RecordWithType2 = CO2Record(secondRecord._1, secondRecord._2, secondRecord._3)
    val output = List(Some(co2RecordWithType), Some(co2RecordWithType2))

    // we call our function here to test our input and output
    assert(ClimateService.parseRawData(list1) == output)
  }

  //@TODO
  test("filterDecemberData") {
    val input = List(
      Some(CO2Record(2022, 12, 400.0)),
      Some(CO2Record(2022, 11, 390.0)),
      Some(CO2Record(2022, 12, 410.0)),
      Some(CO2Record(2022, 10, 380.0)),
      None,
      Some(CO2Record(2022, 12, 420.0))
    )
    val expectedOutput = List(
      CO2Record(2022, 11, 390.0),
      CO2Record(2022, 10, 380.0)
    )
    val actualOutput = ClimateService.filterDecemberData(input)
    assert(actualOutput == expectedOutput)
  }


  test("getMinMax") {
    val input = List(
      CO2Record(2022, 12, 400.0),
      CO2Record(2022, 11, 390.0),
      CO2Record(2022, 12, 410.0),
      CO2Record(2022, 10, 380.0)
    )
    val expectedOutput = (380.0, 410.0)
    val actualOutput = ClimateService.getMinMax(input)
    assert(actualOutput == expectedOutput)
  }

  test("getMinMaxByYear") {
    val input = List(
      CO2Record(2022, 12, 400.0),
      CO2Record(2022, 11, 390.0),
      CO2Record(2022, 12, 410.0),
      CO2Record(2012, 12, 30.0),
      CO2Record(2022, 10, 380.0)
    )
    val expectedOutput = (380.0, 410.0)
    val actualOutput = ClimateService.getMinMaxByYear(input,2022)
    assert(actualOutput == expectedOutput)
  }

  test("estimateCO2LevelsFor2050") {
    val input = List(
      Some(CO2Record(2012, 12, 400.0)),
      Some(CO2Record(2013, 11, 390.0)),
      Some(CO2Record(2014, 12, 410.0)),
      Some(CO2Record(2014, 10, 380.0)),
      Some(CO2Record(2016, 12, 420.0)),
      Some(CO2Record(2016, 12, 430.0)),
      Some(CO2Record(2018, 12, 440.0))
    )
      val estimatedLevel = ClimateService.estimateCO2LevelsFor2050(input)
    assert(estimatedLevel > 420.0)

  }

}

