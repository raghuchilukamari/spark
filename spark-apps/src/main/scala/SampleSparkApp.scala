import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.types.{DoubleType, IntegerType, StringType}
import org.apache.spark.sql.functions.{col, udf}

// For identifying schema
case class Iris (incidentnum:String,
category:String, description:String, dayofweek:String, date:String, time:String,
pddistrict:String, resolution:String, address:String, x:Double, y:Double, pdid:String)

object SampleSparkApp {

  def main(args: Array[String]): Unit = {
    val spark = SparkSession
      .builder()
      .appName("SampleSparkApp")
      .master("local") // .config("spark.master","local")
      .getOrCreate()

    // For definitions of many spark functions
    import spark.implicits._

    // Load data into Dataframe
    val columns=Array("incidentnum",
      "category", "description", "dayofweek", "date", "time",
      "pddistrict", "resolution", "address", "x", "y", "pdid")
    val sfpdDF = spark.read.format("csv")
      .option("inferschema","true")
      .load("/Users/rc/workspaces/spark/Data/sfpd.csv")
      .toDF(columns: _*)

    //Convert Dataframe to Dataset
    val sfpdDS = sfpdDF.as[Iris]
    sfpdDS.show(5)

    //Register Dataset as table -  enables   to query it using SQL.
    sfpdDF.createTempView("sfpd")

    spark.sql("select * from sfpd limit 5").show()

    // Exploring Dataset:

    // top 5 districts with most incidents
    val incByDistDS = sfpdDS.groupBy("pddistrict") // groupby gives a relational grouped dataset, count is more a transformation than action here
      .count()
      .sort($"count".desc)


    incByDistDS.write.format("json")
      .mode("overwrite")
      .save("/Users/rc/workspaces/spark/Data/output/")

    //Caching datasets
    incByDistDS.cache()

    //create UDF:scala for DS operations
    val getYear = udf(
      (s:String) => {
        val year = s.substring(s.lastIndexOf('/')+1)
        year
      }
    )

    val year = sfpdDS.groupBy(getYear(sfpdDF("date")))
      .count()
      .show()

    //create UDF:SQL for SQL like operations
    spark.udf.register("getStr",
      (s:String) => {
        val year = s.substring(s.lastIndexOf('/')+1)
        year
      })

    val numIncByYear = spark.sql("select getStr(date),count(incidentnum) AS countbyYear from sfpd GROUP BY getStr(date) ORDER BY countbyyear DESC limit 5")
    numIncByYear.show()

    // Get current number of partitions
    println(sfpdDS.rdd.getNumPartitions)

    // Repartition
    val sfpdDSR  = sfpdDS.repartition(4)
    println(sfpdDSR.rdd.getNumPartitions)

  }
}
