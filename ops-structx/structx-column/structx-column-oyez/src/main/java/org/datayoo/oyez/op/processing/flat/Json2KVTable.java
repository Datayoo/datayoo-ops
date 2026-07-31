package org.datayoo.oyez.op.processing.flat;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParser;
import org.datayoo.configx.parameter.GroupParameter;
import org.datayoo.datax.sd.ColumnSetMetadata;
import org.datayoo.datax.sd.GeneralColumnSetMetadata;
import org.datayoo.datax.sd.GeneralPlRowSet;
import org.datayoo.datax.sd.PlRowSet;
import org.datayoo.flowx.annotation.Port;
import org.datayoo.flowx.metadata.FlowNodeMetadata;
import org.datayoo.flowx.node.FlowNode;
import org.datayoo.moql.EntityMap;
import org.datayoo.moql.EntityMapImpl;
import org.datayoo.moql.MoqlException;
import org.datayoo.oyez.op.BaseProcessOperator;
import org.datayoo.oyez.op.EngineContext;
import org.datayoo.sengee.SengeeConstants;
import org.datayoo.sengee.annotation.OpDefiner;
import org.datayoo.sengee.datax.flatter.StructureDataFlatter;
import org.datayoo.sengee.exception.OperationInterruptionException;
import org.datayoo.sengee.exception.OperationRuntimeException;
import org.datayoo.sengee.op.exception.OperatorsExceptionConstants;
import org.datayoo.sengee.op.processing.flat.AbstractFlatterDescriptor;
import org.datayoo.sengee.op.processing.flat.Json2KVTableDescriptor;
import org.datayoo.sengee.op.reader.stream.AbstractSemiStructedReaderDescriptor;
import org.datayoo.sengee.op.util.ColumnSetMetadataLibrary;
import org.datayoo.sengee.op.util.MoqlExceptionHelper;
import org.datayoo.sengee.op.util.OperatorsI18nMessageResource;
import org.datayoo.sengee.opp.OperatorProfileConstants;

import java.io.StringReader;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

@OpDefiner(name = "Json2KVTable",
    type = OperatorProfileConstants.OC_READER,
    version = "1.0",
    portrait = "",
    computionFramework = "oyez",
    inputPorts = { @Port(name = "dataIn",
        flowDataType = SengeeConstants.FDT_ANY,
        option = false)
    },
    outputPorts = { @Port(name = "dataOut",
        flowDataType = SengeeConstants.FDT_KV_TABLE,
        option = false)
    },
    parameters = "<parameters></parameters>",
    compoxes = {})
public class Json2KVTable extends BaseProcessOperator {

  protected String dataColumn;

  protected int dataColumnIndex = -1;

  protected Gson gson = new GsonBuilder().create();

  public Json2KVTable(FlowNodeMetadata flowNodeMetadata, FlowNode parent,
      EngineContext engineContext) {
    super(flowNodeMetadata, parent, engineContext);
  }

  @Override
  protected void presetAttributes(GroupParameter groupParameter) {
    dataColumn = groupParameter.getParameterValue(
        Json2KVTableDescriptor.PARAM_DATA_COLUMN);
  }

  @Override
  protected void buildFeatureStates() {

  }

  @Override
  protected void operatorInitialize() {

  }

  @Override
  protected void operatorDestroy() {

  }

  @Override
  protected ColumnSetMetadata createOutputColumnSetMetadata(
      ColumnSetMetadata columnSetMetadata) {
    return ColumnSetMetadataLibrary.createKVTableMetadata();
  }

  @Override
  protected Object[] innerOperate(ColumnSetMetadata columnSetMetadata, int i,
      Object[] objects) {
    return null;
  }

  protected List<Object[]> innerOperate(Object[] objects) {
    String jsonData = (String) objects[dataColumnIndex];
    if (jsonData == null || jsonData.isEmpty())
      return null;
    List<Object[]> outList = new LinkedList<>();
    try {
      List list = gson.fromJson(jsonData, List.class);
      if (isMapElement(list)) {
        for (Object o : list) {
          if (o instanceof Map) {
            outList.addAll(trans2Row((Map) o));
          } else {
            outList.add(createRow(o));
          }
        }
      } else {
        outList.add(createRow(list));
      }
    } catch (Throwable t) {
      Map map = gson.fromJson(jsonData, Map.class);
      outList.addAll(trans2Row(map));
    }
    return outList;
  }

  protected boolean isMapElement(List list) {
    if (list.size() > 0) {
      Object o = list.iterator().next();
      if (o instanceof Map)
        return true;
    }
    return false;
  }

  protected Object[] createRow(Object data) {
    Object[] row = new Object[2];
    row[0] = this.dataColumn;
    row[1] = data;
    return row;
  }

  protected List<Object[]> trans2Row(Map<String, Object> map) {
    List<Object[]> outList = new LinkedList<>();
    for (Map.Entry<String, Object> entry : map.entrySet()) {
      Object[] row = new Object[2];
      row[0] = entry.getKey();
      row[1] = entry.getValue();
      outList.add(row);
    }
    return outList;
  }

  protected void innerOperate() {
    PlRowSet rowSet = this.inputPort.read();
    int i = 0;
    ColumnSetMetadata columnSetMetadata = rowSet.getColumnSetMetadata();
    this.preBindColumnSetMetadata(columnSetMetadata);
    PlRowSet outSet = this.createRowSet(
        this.createOutputColumnSetMetadata(columnSetMetadata));
    for (Object[] row : rowSet.getRows()) {
      if (this.getEngineContext().isTermination()) {
        return;
      }
      try {
        List<Object[]> list = this.innerOperate(row);
        if (list != null && list.size() > 0) {
          outSet.addRows(list);
          if (outSet.getRowsCount() >= getEngineContext().getRowBatchSize()) {
            this.outputPort.write(outSet, this.inputPort.getWaterMark());
            outSet = new GeneralPlRowSet(inputPort.getName(),
                columnSetMetadata);
          }
        }
      } catch (Throwable t) {
        if (!this.reportErrorAndRun(row, t)) {
          throw new OperationInterruptionException(t);
        }
      }
    }
    this.outputPort.write(outSet, this.inputPort.getWaterMark());
  }

  @Override
  protected void preBindColumnSetMetadata(ColumnSetMetadata columnSetMetadata) {
    if (dataColumnIndex == -1) {
      dataColumnIndex = columnSetMetadata.getColumnIndex(dataColumn);
    }
  }

}
