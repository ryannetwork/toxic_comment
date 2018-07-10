package fr.toxic.spark.classification.multiLabelClassification.classifierChains

import fr.toxic.spark.classification.multiLabelClassification.binaryRelevance.ClassifierChainsFactory
import fr.toxic.spark.classification.crossValidation.CrossValidationDecisionTreeTask
import fr.toxic.spark.classification.multiLabelClassification.MultiLabelObject
import fr.toxic.spark.classification.task.DecisionTreeTask
import org.apache.spark.ml.classification.DecisionTreeClassificationModel
import org.apache.spark.sql.DataFrame


class ClassifierChainsDecisionTreeTask(override val labelColumns: Array[String],
                                       override val featureColumn: String,
                                       override val methodValidation: String,
                                       override val savePath: String) extends ClassifierChainsTask(labelColumns, featureColumn,
  methodValidation, savePath) with ClassifierChainsFactory {

  var model: DecisionTreeClassificationModel = _

  override def run(data: DataFrame): ClassifierChainsDecisionTreeTask = {
      prediction = data
      labelColumns.map(label => {
        val newData = createNewDataSet(data, label)
        computeModel(newData, label)
        saveModel(label)
        computePrediction(newData)
      })
      MultiLabelObject.savePrediction(prediction, labelColumns, s"$savePath/prediction")
      MultiLabelObject.multiLabelPrecision(prediction, labelColumns)
      this
    }

  override def computeModel(data: DataFrame, column: String): ClassifierChainsDecisionTreeTask = {
    model = if (methodValidation == "cross_validation") {
      val cv = new CrossValidationDecisionTreeTask(data = data, labelColumn = column,
        featureColumn = featureColumn,
        predictionColumn = s"prediction_$column", pathModel = "",
        pathPrediction = "")
      cv.run()
      cv.getBestModel
    } else {
      val decisionTree = new DecisionTreeTask(labelColumn = column, featureColumn=featureColumn,
        predictionColumn = s"prediction_$column")
      decisionTree.defineModel
      decisionTree.fit(data)
      decisionTree.getModelFit
    }
    this
  }

  override def computePrediction(data: DataFrame): ClassifierChainsDecisionTreeTask = {
    prediction = model.transform(data).drop(Seq("rawPrediction", "probability"): _*)
    this
  }

  override def saveModel(column: String): ClassifierChainsDecisionTreeTask = {
    model.write.overwrite().save(s"$savePath/model/$column")
    this
  }

  override def loadModel(path: String): ClassifierChainsDecisionTreeTask = {
    model = new DecisionTreeTask(featureColumn = featureColumn).loadModel(path).getModelFit
    this
  }

}