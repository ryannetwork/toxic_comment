package fr.toxic.spark

import org.apache.spark.sql.{Column, DataFrame}
import org.apache.spark.sql.functions.col

/**
  * Created by mahjoubi on 13/06/18.
  */
class BinaryRelevanceTask(val columns: Array[String], val savePath: String, val featureColumn: String = "tf_idf") {

  def run(data: DataFrame): Unit = {
    var prediction: DataFrame = createFeatures(data)
    columns.map(column => {
      println(s"column: ${column}")
      val labelFeatures = createLabel(prediction, column)
      prediction = computeModel(labelFeatures, column)
    })
    savePrediction(prediction)
  }

  def createFeatures(data: DataFrame): DataFrame = {
    data.withColumnRenamed(featureColumn, "features")
  }

  def createLabel(data: DataFrame, column: String): DataFrame = {
    data.withColumnRenamed(column, s"label_${column}")
  }

  def computeModel(data: DataFrame, column: String): DataFrame = {
    val logisticRegression = new LogisticRegressionTask(labelColumn = s"label_${column}",
      predictionColumn = s"prediction_${column}")
    logisticRegression.fitModel(data)
    logisticRegression.transformModel(data)
    logisticRegression.getPrediction().drop("probability").drop("rawPrediction")
  }

  def savePrediction(data: DataFrame): Unit = {
//    val columnsToKeep: Set[Column] = (data.columns.toSet
//      -- Set(columns: _*)
//      -- Set("comment_text", "clean_tokens", "words", "tf", "features")).map(name => col(name))

    val columnsToKeep: Set[String] = (Set("id")
      ++ Set(columns).map(name => s"label_${name.toString()}")
      ++ Set(columns).map(name => s"prediction_${name}"))
//      .map(name => col(name))

    print(columnsToKeep)
//    data.select(columnsToKeep.toSeq: _*).write.option("header", "true").mode("overwrite").csv(s"${savePath}/prediction")
  }

}