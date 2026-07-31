package org.datayoo.sengee.op.processing.v.transformer;

import org.datayoo.base.types.DataTypeUtils;
import org.datayoo.configx.parameter.RowParameter;
import org.datayoo.configx.parameter.TableParameter;
import org.datayoo.datax.sd.ColumnMetadata;
import org.datayoo.datax.sd.ColumnSetMetadata;
import org.datayoo.datax.sd.GeneralColumnMetadata;
import org.datayoo.datax.sd.PlRowSet;
import org.datayoo.flowx.annotation.Port;
import org.datayoo.flowx.annotation.TagPair;
import org.datayoo.flowx.metadata.FlowNodeMetadata;
import org.datayoo.flowx.node.FlowNode;
import org.datayoo.flowx.node.FlowPort;
import org.datayoo.sengee.SengeeConstants;
import org.datayoo.sengee.annotation.OpDefiner;
import org.datayoo.sengee.exception.OperationRuntimeException;
import org.datayoo.sengee.op.SengeeOperatorConstants;
import org.datayoo.sengee.op.exception.OperatorsExceptionConstants;
import org.datayoo.sengee.op.processing.AbstractProcessingDescriptor;
import org.datayoo.sengee.op.util.OperatorsI18nMessageResource;
import org.datayoo.sengee.opd.CompilationContext;
import org.datayoo.sengee.opp.OperatorProfileConstants;

import java.util.ArrayList;

@OpDefiner(name = "ValuesSplitter",
    type = OperatorProfileConstants.OC_PROCESS,
    version = "1.0",
    computionFramework = "sengee",
    tags = {
        @TagPair(name = SengeeConstants.TAG_OP_CATS, value = "rowset,structure")
    },
    inputPorts = { @Port(name = SengeeOperatorConstants.PORT_DATA_IN,
        flowDataType = SengeeConstants.FDT_ANY,
        option = false)
    },
    outputPorts = { @Port(name = SengeeOperatorConstants.PORT_DATA_OUT,
        flowDataType = SengeeConstants.FDT_ANY,
        option = true)
    },
    parameters =
        "<parameters><parametergroup name=\"general\" c_Alias=\"一般\" c_Compox=\"sightx-tab\">"
            + "<parametertable name=\"columnSet\" c_Alias=\"待拆分列集合\"><head>"
            + "<parameter name=\"columnName\" c_Option=\"false\" c_Alias=\"列名\" c_Compox=\"sengee-string-column-selector\"></parameter>"
            + "<parameter name=\"newColumnLength\" c_Option=\"false\" c_Alias=\"分割后最大列数目\" c_Compox=\"sightx-integer\">-1</parameter>"
            + "</head>" + "</parametertable>"
            + "<parameter name=\"separator\" c_Option=\"false\" c_Alias=\"分隔符\"></parameter>"
            + "</parametergroup></parameters>",
    compoxes = {})
public class ValuesSplitterDescriptor extends AbstractProcessingDescriptor {

  public static final String PARAM_SEPARATOR = "separator";

  public static final String PARAM_NEW_COLUMN_LENGTH = "newColumnLength";

  protected Object[][] splitColumns;

  public ValuesSplitterDescriptor(FlowNodeMetadata flowNodeMetadata,
      FlowNode parent, CompilationContext compilationContext) {
    super(flowNodeMetadata, parent, compilationContext);
  }

  @Override
  protected void readParameters() {
    TableParameter columnSetParameter = getColumnSetParameter();
    splitColumns = new Object[columnSetParameter.getParameters().size()][2];
    int i = 0;
    columns = new ArrayList<>(columnSetParameter.getParameters().size());
    for (RowParameter rowParameter : columnSetParameter.getParameters()) {
      String columnName = rowParameter.getParameterValue(PARAM_COLUMN_NAME, "");
      if (columnName.isEmpty())
        continue;
      columns.add(columnName);
      int length = rowParameter.getParameterValueAsInt(PARAM_NEW_COLUMN_LENGTH,
          -1);
      splitColumns[i][0] = columnName;
      splitColumns[i][1] = length;
      i++;
    }
  }

  @Override
  protected ColumnSetMetadata compileCsmOfOutputPort(
      FlowPort<PlRowSet> flowPort) {
    ColumnSetMetadata metadata = getInputColumnSetMetadata();
    readParameters();
    return splitColumnSetMetadata(metadata);
  }

  @Override
  public void validateParameters() {
    super.validateParameters();
    String pattern = parameters.getParameterValue(PARAM_SEPARATOR, "");
    if (pattern.isEmpty()) {
      throw new OperationRuntimeException(OperatorsI18nMessageResource.format(
          OperatorsExceptionConstants.ECM_PARAM_EMPTY, this.alias,
          PARAM_SEPARATOR));
    }
  }

  protected ColumnSetMetadata splitColumnSetMetadata(
      ColumnSetMetadata metadata) {
    if (null != metadata) {
      if (columns.size() == 0)
        return metadata;
      for (int i = 0; i < splitColumns.length; i++) {
        ColumnMetadata columnMetadata = metadata.getColumn(
            (String) splitColumns[i][0]);
        int newColumnLength = (int) splitColumns[i][1];
        if (newColumnLength > 0) {
          for (int j = 1; j <= newColumnLength; j++) {
            ColumnMetadata newColumnMetadata = new GeneralColumnMetadata(
                columnMetadata.getName() + "_" + j);
            newColumnMetadata.setType(DataTypeUtils.STRING_TYPE);
            metadata.addColumn(newColumnMetadata);
          }
        } else {
          ColumnMetadata newColumnMetadata = new GeneralColumnMetadata(
              columnMetadata.getName() + "_[n]");
          newColumnMetadata.setType(DataTypeUtils.STRING_TYPE);
          metadata.addColumn(newColumnMetadata);
        }
      }
      return metadata;
    }
    return null;
  }
}
