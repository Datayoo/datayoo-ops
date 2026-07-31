package org.datayoo.oyez.op.processing.flat;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import org.apache.log4j.Level;
import org.datayoo.base.lang.Pair;
import org.datayoo.configx.parameter.GroupParameter;
import org.datayoo.configx.parameter.RowParameter;
import org.datayoo.configx.parameter.TableParameter;
import org.datayoo.datax.listener.DataListener;
import org.datayoo.datax.sd.ColumnSetMetadata;
import org.datayoo.datax.sd.GeneralColumnSetMetadata;
import org.datayoo.datax.sd.PlRowSet;
import org.datayoo.flowx.annotation.Port;
import org.datayoo.flowx.metadata.FlowNodeMetadata;
import org.datayoo.flowx.node.FlowNode;
import org.datayoo.flowx.node.FlowPort;
import org.datayoo.moql.EntityMap;
import org.datayoo.moql.EntityMapImpl;
import org.datayoo.moql.MoqlException;
import org.datayoo.oyez.op.EngineContext;
import org.datayoo.oyez.op.OutputColumnSetMetadataCreator;
import org.datayoo.oyez.op.reader.stream.AbstractSemiStructedReader;
import org.datayoo.sengee.SengeeConstants;
import org.datayoo.sengee.annotation.OpDefiner;
import org.datayoo.sengee.datax.flatter.GeneralFlatter;
import org.datayoo.sengee.datax.flatter.StructureDataFlatter;
import org.datayoo.sengee.datax.flatter.json.JsonFlatter;
import org.datayoo.sengee.datax.flatter.xml.XmlFlatter;
import org.datayoo.sengee.exception.OperationInterruptionException;
import org.datayoo.sengee.exception.OperationRuntimeException;
import org.datayoo.sengee.op.exception.OperatorsExceptionConstants;
import org.datayoo.sengee.op.processing.flat.ObjectFlatterDescriptor;
import org.datayoo.sengee.op.reader.stream.AbstractSemiStructedReaderDescriptor;
import org.datayoo.sengee.op.util.MoqlExceptionHelper;
import org.datayoo.sengee.op.util.OperatorsI18nMessageResource;
import org.datayoo.sengee.opa.InputStreamEntry;
import org.datayoo.sengee.opp.OperatorProfileConstants;
import org.dom4j.Element;

import java.util.List;

/**
 * 字符串剪切
 *
 * @author hhn
 */
@OpDefiner(name = "ObjectFlatter",
    type = OperatorProfileConstants.OC_PROCESS,
    version = "1.0",
    computionFramework = "oyez",
    inputPorts = { @Port(name = "dataIn",
        flowDataType = SengeeConstants.FDT_ANY,
        option = false)
    },
    outputPorts = { @Port(name = "dataOut",
        flowDataType = SengeeConstants.FDT_ANY,
        option = false)
    },
    parameters = "",
    compoxes = {})
public class ObjectFlatter extends AbstractSemiStructedReader {

  protected String column;
  protected int columnIndex = -1;
  protected ColumnSetMetadata outColumnSetMetadata;
  protected StructureDataFlatter dataFlatter;

  protected String[] retains;

  protected int[] retainIndexes;

  protected OutputColumnSetMetadataCreator outputColumnSetMetadataCreator = new InnerOutputColumnSetMetadataCreator();

  public ObjectFlatter(FlowNodeMetadata operatorMetadata, FlowNode parent,
      EngineContext engineContext) {
    super(operatorMetadata, parent, engineContext);
  }

  @Override
  protected StructureDataFlatter createDataFlatter() {
    //    try {
    //      return new JsonFlatter(this.structureCollectionMetadata);
    //    } catch (MoqlException e) {
    //      String operand = MoqlExceptionHelper.extractOperand(e.getMessage());
    //      throw new OperationRuntimeException(OperatorsI18nMessageResource.format(
    //          OperatorsExceptionConstants.ECM_OPD_OPERAND_BUILD_FAILED, this.alias,
    //          operand));
    //}
    return null;
  }

  @Override
  protected EntityMap loadData(InputStreamEntry inputStreamEntry) {
    return null;
  }

  @Override
  protected void presetAttributes(GroupParameter groupParameter) {
    column = groupParameter.getParameterValue(
        ObjectFlatterDescriptor.PARAM_OBJECT_COLUMN);
    columnSet = loadColumnSetFromParameters();
    TableParameter columnSetParameter = (TableParameter) this.parameters.getParameter(
        ObjectFlatterDescriptor.PARAM_COLUMN_SET);
    List<RowParameter> rowParameters = columnSetParameter.getParameters();
    retains = new String[rowParameters.size()];
    retainIndexes = new int[rowParameters.size()];
    int i = 0;
    for (RowParameter rowParameter : rowParameters) {
      String column = rowParameter.getParameterValue(
          ObjectFlatterDescriptor.PARAM_COLUMN_NAME);
      retains[i] = column;
      i++;
    }
  }

  @Override
  protected void buildFeatureStates() {

  }

  @Override
  protected void innerOperate() {
    PlRowSet rowSet = this.inputPort.read();
    ColumnSetMetadata columnSetMetadata = rowSet.getColumnSetMetadata();
    int i = 0;
    for (Object[] row : rowSet.getRows()) {
      if (this.getEngineContext().isTermination()) {
        return;
      }
      try {
        this.innerOperate(columnSetMetadata, i++, row);
      } catch (Throwable var7) {
        if (!this.reportErrorAndRun(row, var7)) {
          throw new OperationInterruptionException(var7);
        }
      }
    }
  }

  @Override
  protected String getEntityType(String s) {
    return null;
  }

  protected ColumnSetMetadata createColumnSetMetadata(
      ColumnSetMetadata columnSetMetadata) {
    GeneralColumnSetMetadata output = new GeneralColumnSetMetadata(
        SengeeConstants.FDT_ANY);
    for (String columnName : retains) {
      output.getColumns().add(columnSetMetadata.getColumn(columnName));
    }
    if (columnSet != null && columnSet.size() > 0) {
      output.getColumns().addAll(columnSet);
    } else {
      throw new IllegalArgumentException(OperatorsI18nMessageResource.format(
          OperatorsExceptionConstants.ECM_OPD_COLUMNSET_ISNOT_SET, this.alias));
    }
    return output;
  }

  protected void innerOperate(ColumnSetMetadata columnSetMetadata, int i,
      Object[] row) {
    if (columnIndex == -1) {
      columnIndex = columnSetMetadata.getColumnIndex(column);
      outColumnSetMetadata = createColumnSetMetadata(columnSetMetadata);
      for (int j = 0; j < retains.length; j++) {
        retainIndexes[j] = columnSetMetadata.getColumnIndex(retains[j]);
      }
    }
    Object object = row[columnIndex];
    if (object == null) {
      this.getEngineContext().log(this.alias, Level.WARN,
          String.format("data is null,row is %s", new Gson().toJson(row)),null);
      return;
    }
    if (object.getClass().isArray()) {
      innerOperate((Object[]) object, row);
    } else {
      innerOperate(object, row);
    }
  }

  protected void innerOperate(Object[] objects, Object[] row) {
    for (Object object : objects) {
      innerOperate(object, row);
    }
  }

  protected void innerOperate(Object object, Object[] row) {
    InnerDataListener dataListener = new InnerDataListener(
        getEngineContext().getRowBatchSize(), row);
    if (object instanceof Element) {
      createXmlDataFlatter();
      dataFlatter.setDataListener(dataListener);
      EntityMap entityMap = loadXmlData((Element) object);
      dataFlatter.structure(entityMap);
    } else if (object instanceof JsonElement) {
      createJsonDataFlatter();
      dataFlatter.setDataListener(dataListener);
      EntityMap entityMap = loadJsonData((JsonElement) object);
      dataFlatter.structure(entityMap);
    } else {
      createGeneralDataFlatter();
      dataFlatter.setDataListener(dataListener);
      EntityMapImpl entityMap = new EntityMapImpl();
      entityMap.putEntity(
          AbstractSemiStructedReaderDescriptor.OBJECT_ENTITY_NAME, object);
      dataFlatter.structure(entityMap);
    }
  }

  protected ColumnSetMetadata createColumnSetMetadata(FlowPort flowPort) {
    GeneralColumnSetMetadata columnSetMetadata = new GeneralColumnSetMetadata(
        flowPort.getFlowDataType());
    if (columnSet != null && columnSet.size() > 0) {
      columnSetMetadata.getColumns().addAll(columnSet);
    } else {
      throw new IllegalArgumentException(OperatorsI18nMessageResource.format(
          OperatorsExceptionConstants.ECM_OPD_COLUMNSET_ISNOT_SET, this.alias));
    }
    return columnSetMetadata;
  }

  protected EntityMap loadXmlData(Element element) {
    EntityMapImpl entityMap = new EntityMapImpl();
    entityMap.putEntity(element.getName(), element);
    return entityMap;
  }

  protected void createXmlDataFlatter() {
    if (dataFlatter != null) {
      return;
    }
    try {
      dataFlatter = new XmlFlatter(this.structureCollectionMetadata);
    } catch (MoqlException e) {
      String operand = MoqlExceptionHelper.extractOperand(e.getMessage());
      throw new OperationRuntimeException(OperatorsI18nMessageResource.format(
          OperatorsExceptionConstants.ECM_OPD_OPERAND_BUILD_FAILED, this.alias,
          operand));
    }
  }

  protected EntityMap loadJsonData(JsonElement jsonElement) {
    EntityMapImpl entityMap = new EntityMapImpl();
    entityMap.putEntity(AbstractSemiStructedReaderDescriptor.OBJECT_ENTITY_NAME,
        jsonElement);
    return entityMap;
  }

  protected void createJsonDataFlatter() {
    if (dataFlatter != null) {
      return;
    }
    try {
      dataFlatter = new JsonFlatter(this.structureCollectionMetadata);
    } catch (MoqlException e) {
      String operand = MoqlExceptionHelper.extractOperand(e.getMessage());
      throw new OperationRuntimeException(OperatorsI18nMessageResource.format(
          OperatorsExceptionConstants.ECM_OPD_OPERAND_BUILD_FAILED, this.alias,
          operand));
    }
  }

  protected void createGeneralDataFlatter() {
    if (dataFlatter != null) {
      return;
    }
    try {
      dataFlatter = new GeneralFlatter(this.structureCollectionMetadata);
    } catch (MoqlException e) {
      String operand = MoqlExceptionHelper.extractOperand(e.getMessage());
      throw new OperationRuntimeException(OperatorsI18nMessageResource.format(
          OperatorsExceptionConstants.ECM_OPD_OPERAND_BUILD_FAILED, this.alias,
          operand));
    }
  }

  protected class InnerDataListener
      implements DataListener<Pair<String, Object>> {

    protected int size = 0;

    protected int batchSize = 0;

    protected Object[] row;

    public InnerDataListener(int batchSize, Object[] row) {
      this.batchSize = batchSize;
      this.row = row;
    }

    @Override
    public void onData(Pair<String, Object> stringObjectPair) {
      throw new UnsupportedOperationException("");
    }

    @Override
    public void onData(List<Pair<String, Object>> list) {
      Object[] result = new Object[retains.length + columnSet.size()];
      for (int i = 0; i < retainIndexes.length; i++) {
        result[i] = row[retainIndexes[i]];
      }
      int i = retains.length;
      for (Pair<String, Object> pair : list) {
        result[i++] = pair.getValue();
      }
      outputPort.writeWithRowBatch(outputColumnSetMetadataCreator, result,
          inputPort.getWaterMark(), batchSize);
      size++;
    }

    public int getSize() {
      return size;
    }
  }

  protected class InnerOutputColumnSetMetadataCreator
      implements OutputColumnSetMetadataCreator {

    @Override
    public ColumnSetMetadata createColumnSetMetadata(String s) {
      return new GeneralColumnSetMetadata(outColumnSetMetadata);
    }
  }
}
