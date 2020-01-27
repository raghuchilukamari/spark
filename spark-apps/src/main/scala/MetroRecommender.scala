import org.apache.spark.ml.feature.BucketedRandomProjectionLSH
import org.apache.spark.ml.linalg.Vectors
import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.functions._

object MetroRecommender{

  def main(args: Array[String]): Unit = {

    val spark = SparkSession
      .builder()
      .appName("metro-recommender")
      .master("local[*]")
      .getOrCreate()


    var metroDF = spark.read.format("csv")
      .option("header","true")
      .option("inferschema","true")
      .load("/Users/rc/workspaces/spark/Data/NYC_Transit_Subway_Entrance_And_Exit_Data.csv")

    metroDF.cache()

    var stationDF = metroDF.select("Line","Station Name","Station Latitude","Station Longitude")

    def locVector(d1:Double, d2:Double) : org.apache.spark.ml.linalg.Vector = Vectors.dense(d1,d2)
    val udf_locVector = udf(locVector _)

    stationDF = stationDF
      .withColumn("sta_vec",udf_locVector($"Station Latitude",$"Station Longitude"))
        .withColumn("Station", concat($"Station Name",lit(" ("), $"Line", lit(")")))
        .drop("Line","Station Name")

    stationDF.cache()

    stationDF = stationDF.distinct()

    var dfA = stationDF.select("Station","sta_vec").toDF("id","features")
    var dfB = stationDF.select("Station","sta_vec").toDF("id","features")

    val brp = new BucketedRandomProjectionLSH()
      .setBucketLength(2.0)
      .setNumHashTables(3)
      .setInputCol("features")
      .setOutputCol("hashes")

    val model = brp.fit(dfA)

    model.transform(dfA).show()

    var approxSimilarityDF = model.approxSimilarityJoin(dfA, dfB, 1.0, "EuclideanDistance")
      .select(col("datasetA.id").alias("idA"),
        col("datasetB.id").alias("idB"),
        col("EuclideanDistance"))

    var approxDF = approxSimilarityDF.withColumn("round_dis", round(($"EuclideanDistance"*100),1))

    approxDF.cache()






















  }
}

