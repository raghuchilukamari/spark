import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.functions._

object SampleSparkApp {
  def main(args: Array[String]): Unit = {
    val spark = SparkSession
      .builder()
      .appName("SampleSparkApp")
      .config("spark.master","local")
      .getOrCreate();
    val df = spark.read.csv("/Users/rc/Documents/workspace/spark-sample/iris.csv");
    df.show(5);

  }



}
