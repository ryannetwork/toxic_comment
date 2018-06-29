package fr.toxic.spark

import org.apache.spark.ml.classification.{LogisticRegression, LogisticRegressionModel}
import org.apache.spark.sql.DataFrame

/**
  * Created by mahjoubi on 12/06/18.
  */
class LogisticRegressionTask(val labelColumn: String = "label",
                             val featureColumn: String = "features",
                             val predictionColumn: String = "prediction") {


  var model: LogisticRegression = _
  var modelFit: LogisticRegressionModel = _
  var transform: DataFrame = _

  def getModelFit(): LogisticRegressionModel = {
    modelFit
  }

  def defineModel(): LogisticRegressionTask= {
    model = new LogisticRegression()
      .setFeaturesCol(featureColumn)
      .setLabelCol(labelColumn)
      .setPredictionCol(predictionColumn)
    this
  }

  def fit(data: DataFrame): LogisticRegressionTask = {
    modelFit = getModel().fit(data)
    this
  }

  def getModel(): LogisticRegression = {
    model
  }

  def transform(data: DataFrame): LogisticRegressionTask = {
    transform = modelFit.transform(data)
    this
  }

  def saveModel(path: String): LogisticRegressionTask = {
    model.save(path)
    this
  }

  def setRegParam(value: Double): LogisticRegressionTask = {
    model.setRegParam(value)
    this
  }

  def getRegParam(): Double = {
    model.getRegParam
  }

  def loadModel(path: String): LogisticRegressionTask = {
    modelFit = LogisticRegressionModel.load(path)
    this
  }

  def getTransform(): DataFrame = {
    transform
  }
}
