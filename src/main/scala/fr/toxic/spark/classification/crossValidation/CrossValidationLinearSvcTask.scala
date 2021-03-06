package fr.toxic.spark.classification.crossValidation

import fr.toxic.spark.classification.task.{ClassificationModelFactory, CrossValidationModelFactory, LinearSvcTask}
import org.apache.spark.ml.classification.{LinearSVC, LinearSVCModel}
import org.apache.spark.ml.evaluation.{BinaryClassificationEvaluator, Evaluator}
import org.apache.spark.ml.param.ParamMap
import org.apache.spark.ml.tuning.{CrossValidator, CrossValidatorModel, ParamGridBuilder}
import org.apache.spark.sql.DataFrame


class CrossValidationLinearSvcTask(val data: DataFrame,
                                   val labelColumn: String,
                                   val featureColumn: String,
                                   val predictionColumn: String,
                                   val pathModel: String,
                                   val pathPrediction: String) {

  var estimator: LinearSVC = _
  var evaluator: BinaryClassificationEvaluator = _
  var paramGrid: Array[ParamMap] = _
  var crossValidator: CrossValidator = _
  var crossValidatorModel: CrossValidatorModel = _

  def run(): CrossValidationLinearSvcTask = {
    defineEstimator()
    defineGridParameters()
    defineEvaluator()
    defineCrossValidatorModel()
    fit()
  }

  def defineEstimator(): CrossValidationLinearSvcTask = {
    estimator = new LinearSvcTask(labelColumn=labelColumn,
                                  featureColumn=featureColumn,
                                  predictionColumn=predictionColumn).defineModel.getModel
    this
  }

  def defineGridParameters(): CrossValidationLinearSvcTask = {
      paramGrid = new ParamGridBuilder()
        .addGrid(estimator.regParam, Array(0.0, 0.001, 0.01, 0.1, 1.0, 10.0))
        .addGrid(estimator.fitIntercept, Array(true, false))
        .addGrid(estimator.standardization, Array(true, false))
        .build()
    this
  }

   def defineEvaluator(): CrossValidationLinearSvcTask = {
      evaluator = new BinaryClassificationEvaluator()
        .setRawPredictionCol(predictionColumn)
        .setLabelCol(labelColumn)
        .setMetricName("areaUnderROC")
    this}

  def defineCrossValidatorModel(): CrossValidationLinearSvcTask = {
    crossValidator = new CrossValidator()
      .setEvaluator(evaluator)
      .setEstimatorParamMaps(paramGrid)
      .setEstimator(estimator)
    this
  }

  def fit(): CrossValidationLinearSvcTask = {
    crossValidatorModel = crossValidator.fit(data)
    this
  }

  def transform(data: DataFrame): DataFrame = {
    crossValidatorModel.transform(data)
  }

  def getEstimator: LinearSVC = {
    estimator
  }

  def getBestModel: LinearSVCModel = {
    crossValidatorModel.bestModel.asInstanceOf[LinearSVCModel]
  }

  def setGridParameters(grid: Array[ParamMap]): CrossValidationLinearSvcTask = {
     paramGrid = grid
     this
   }
}