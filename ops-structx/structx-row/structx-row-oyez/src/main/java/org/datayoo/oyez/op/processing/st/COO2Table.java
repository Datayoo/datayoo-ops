package org.datayoo.oyez.op.processing.st;

import org.datayoo.base.types.DataTypeUtils;
import org.datayoo.configx.parameter.GroupParameter;
import org.datayoo.configx.parameter.RowParameter;
import org.datayoo.configx.parameter.TableParameter;
import org.datayoo.datax.sd.*;
import org.datayoo.flowx.annotation.Port;
import org.datayoo.flowx.metadata.FlowNodeMetadata;
import org.datayoo.flowx.node.FlowNode;
import org.datayoo.oyez.op.BaseProcessOperator;
import org.datayoo.oyez.op.EngineContext;
import org.datayoo.sengee.SengeeConstants;
import org.datayoo.sengee.annotation.OpDefiner;
import org.datayoo.sengee.op.SengeeOperatorConstants;
import org.datayoo.sengee.op.processing.st.COO2TableDescriptor;
import org.datayoo.sengee.opp.OperatorProfileConstants;

import java.util.*;
/**
 * csm格式数据转为正常的表格
 *
 * @author hhn
 */
@OpDefiner(name = "COO2Table",
    type = OperatorProfileConstants.OC_PROCESS,
    version = "1.0",
    computionFramework = "oyez",
    replica = -1,
    inputPorts = { @Port(name = SengeeOperatorConstants.PORT_DATA_IN,
        flowDataType = SengeeConstants.FDT_FLAT_TABLE,
        option = false)
    },
    outputPorts = { @Port(name = SengeeOperatorConstants.PORT_DATA_OUT,
        flowDataType = SengeeConstants.FDT_ANY,
        option = false)
    },
    parameters = "",
    compoxes = {})
public class COO2Table extends BaseProcessOperator {

  private String type;
  private String headerType;
  private int colHeaderIndex = -1;
  private int colStartIndex = -1;
  private int rolHeaderIndex = -1;
  private int rolStartIndex = -1;
  private Object[][] columnSet;

  public COO2Table(FlowNodeMetadata operatorMetadata, FlowNode parent,
      EngineContext engineContext) {
    super(operatorMetadata, parent, engineContext);
  }

  @Override
  protected void presetAttributes(GroupParameter groupParameter) {
    type = groupParameter.getParameterValue(COO2TableDescriptor.PARAM_TYPE);
    headerType = groupParameter.getParameterValue(
        COO2TableDescriptor.PARAM_HEADER_TYPE);
    if (type.equals("RowMode")) {
      if (headerType.equals("ValMode")) {
        rolHeaderIndex = groupParameter.getParameterValueAsInt(
            COO2TableDescriptor.PARAM_ROW_HEADER_INDEX);
        rolStartIndex = groupParameter.getParameterValueAsInt(
            COO2TableDescriptor.PARAM_ROW_VAL_INDEX);
      } else {
        readColumnSet(groupParameter);
        Arrays.sort(columnSet, new Comparator<Object[]>() {
          @Override
          public int compare(Object[] o1, Object[] o2) {
            return (int) o1[2] - (int) o2[2];
          }
        });
      }
    } else {
      if (headerType.equals("ValMode")) {
        colHeaderIndex = groupParameter.getParameterValueAsInt(
            COO2TableDescriptor.PARAM_COL_HEADER_INDEX);
        colStartIndex = groupParameter.getParameterValueAsInt(
            COO2TableDescriptor.PARAM_COL_VAL_INDEX);
      } else {
        readColumnSet(groupParameter);
        Arrays.sort(columnSet, new Comparator<Object[]>() {
          @Override
          public int compare(Object[] o1, Object[] o2) {
            return (int) o1[2] - (int) o2[2];
          }
        });
      }
    }
  }

  @Override
  protected void innerOperate() {
    PlRowSet rowSet = this.inputPort.read();
    ColumnSetMetadata columnSetMetadata = rowSet.getColumnSetMetadata();
    if (type.equals("RowMode")) {
      handleRowMode(rowSet.getRows());
    } else {
      handleColMode(rowSet.getRows());
    }
  }

  @Override
  protected ColumnSetMetadata createOutputColumnSetMetadata(
      ColumnSetMetadata columnSetMetadata) {
    return null;
  }

  @Override
  protected Object[] innerOperate(ColumnSetMetadata columnSetMetadata, int i,
      Object[] objects) {
    return new Object[0];
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

  private ColumnSetMetadata createOutputColumnSetMetadata(
      List<String> headers) {
    ColumnSetMetadata columnSetMetadata = new GeneralColumnSetMetadata(
        "/");
    List<String> names = new ArrayList<>();
    for (Object o : headers) {
      long count = names.stream().filter(v -> v.equals(o)).count();
      if (count == 0) {
        names.add(o.toString());
        ColumnMetadata columnMetadata = new GeneralColumnMetadata(o.toString());
        columnMetadata.setType(DataTypeUtils.STRING_TYPE);
        columnSetMetadata.addColumn(columnMetadata);
      } else {
        names.add(o.toString());
        ColumnMetadata columnMetadata = new GeneralColumnMetadata(
            o.toString() + count);
        columnMetadata.setType(DataTypeUtils.STRING_TYPE);
        columnSetMetadata.addColumn(columnMetadata);
      }
    }
    return columnSetMetadata;
  }

  private void readColumnSet(GroupParameter groupParameter){
    TableParameter tableParameter = (TableParameter) groupParameter.getParameter(
        COO2TableDescriptor.PARAM_COL_SET);
    columnSet = new Object[tableParameter.getParameters().size()][3];
    for (int i = 0; i < tableParameter.getParameters().size(); i++) {
      RowParameter rowParameter = tableParameter.getParameters().get(i);
      Object[] row = new Object[3];
      row[0] = rowParameter.getParameterValue(
          COO2TableDescriptor.PARAM_COL_NAME);
      row[1] = rowParameter.getParameterValue(
          COO2TableDescriptor.PARAM_COL_TYPE);
      row[2] = rowParameter.getParameterValueAsInt(
          COO2TableDescriptor.PARAM_INDEX);
      columnSet[i] = row;
    }
  }

  private void handleRowMode(List<Object[]> rows){
    rows.sort(new Comparator<Object[]>() {
      @Override
      public int compare(Object[] o1, Object[] o2) {
        if (o1[1] != o2[1]) {
          return (int) o1[1] - (int) o2[1];
        } else {
          return (int) o1[2] - (int) o2[2];
        }
      }
    });
    if (headerType.equals("ValMode")) {
      ColumnSetMetadata outputColumnSetMetadata = buildValueColSet(rows,1);
      PlRowSet output = new GeneralPlRowSet(this.outputPort.getName(),
          outputColumnSetMetadata);
      Object[] data = new Object[outputColumnSetMetadata.getColumnsCount()];
      for (Object[] row : rows) {
        if ((int) row[1] >= rolStartIndex) {
          data[(int) row[2]] = row[3];
          if (data[outputColumnSetMetadata.getColumnsCount() - 1] != null) {
            output.addRow(data);
            data = new Object[outputColumnSetMetadata.getColumnsCount()];
          }
        }
      }
      outputPort.write(output, inputPort.getWaterMark());
    } else {
      ColumnSetMetadata columnSetMetadata = buildManualColSet();
      PlRowSet output = new GeneralPlRowSet(this.outputPort.getName(),
          columnSetMetadata);
      Object[] data = new Object[columnSetMetadata.getColumnsCount()];
      for (Object[] row : rows) {
        if ((Integer) row[1] >= rolStartIndex) {
          for(int i=0;i<columnSet.length;i++){
            Object[] column = columnSet[i];
            if(row[2] == column[2]){
              data[i] = row[3];
              break;
            }
          }
          if (data[columnSetMetadata.getColumnsCount() - 1] != null) {
            output.addRow(data);
            data = new Object[columnSetMetadata.getColumnsCount()];
          }
        }
      }
      outputPort.write(output, inputPort.getWaterMark());
    }
  }

  protected void handleColMode(List<Object[]> rows){
    rows.sort(new Comparator<Object[]>() {
      @Override
      public int compare(Object[] o1, Object[] o2) {
        if (o1[2] != o2[2]) {
          return (int) o1[2] - (int) o2[2];
        } else {
          return (int) o1[1] - (int) o2[1];
        }
      }
    });
    if (headerType.equals("ValMode")) {
      ColumnSetMetadata outputColumnSetMetadata = buildValueColSet(rows,2);
      PlRowSet output = new GeneralPlRowSet(this.outputPort.getName(),
          outputColumnSetMetadata);
      Object[] data = new Object[outputColumnSetMetadata.getColumnsCount()];
      for (Object[] row : rows) {
        if ((int) row[2] >= colStartIndex) {
          data[(Integer) row[1]] = row[3];
          if (data[outputColumnSetMetadata.getColumnsCount() - 1] != null) {
            output.addRow(data);
            data = new Object[outputColumnSetMetadata.getColumnsCount()];
          }
        }
      }
      outputPort.write(output, inputPort.getWaterMark());
    } else {
      ColumnSetMetadata columnSetMetadata = buildManualColSet();
      PlRowSet output = new GeneralPlRowSet(this.outputPort.getName(),
          columnSetMetadata);
      Object[] data = new Object[columnSetMetadata.getColumnsCount()];
      for (Object[] row : rows) {
        if ((int) row[1] >= rolStartIndex) {
          for(int i=0;i<columnSet.length;i++){
            Object[] column = columnSet[i];
            if(row[1] == column[2]){
              data[i] = row[3];
              break;
            }
          }
          if (data[columnSetMetadata.getColumnsCount() - 1] != null) {
            output.addRow(data);
            data = new Object[columnSetMetadata.getColumnsCount()];
          }
        }
      }
      outputPort.write(output, inputPort.getWaterMark());
    }
  }

  protected ColumnSetMetadata buildManualColSet(){
    ColumnSetMetadata outputColumnSetMetadata = new GeneralColumnSetMetadata(
        "/");
    for (Object[] column : columnSet) {
      ColumnMetadata columnMetadata = new GeneralColumnMetadata(
          (String) column[0]);
      columnMetadata.setType(
          DataTypeUtils.createDataType((String) column[1]));
      outputColumnSetMetadata.addColumn(columnMetadata);
    }
    return outputColumnSetMetadata;
  }

  protected ColumnSetMetadata buildValueColSet(List<Object[]> rows,int index){
    List<String> headers = new ArrayList<>();
    for (Object[] row : rows) {
      if (index == 2 && colHeaderIndex == (int) row[index]) {
        headers.add((String) row[3]);
      }
      if (index == 1 && rolHeaderIndex == (int) row[index]) {
        headers.add((String) row[3]);
      }
    }
    return createOutputColumnSetMetadata(
        headers);
  }


}
