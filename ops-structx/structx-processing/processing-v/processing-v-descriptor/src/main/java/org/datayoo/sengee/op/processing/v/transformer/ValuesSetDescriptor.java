package org.datayoo.sengee.op.processing.v.transformer;

import org.datayoo.base.types.DataType;
import org.datayoo.base.types.DataTypeUtils;
import org.datayoo.configx.parameter.RowParameter;
import org.datayoo.configx.parameter.TableParameter;
import org.datayoo.datax.sd.ColumnSetMetadata;
import org.datayoo.datax.sd.PlRowSet;
import org.datayoo.flowx.annotation.Port;
import org.datayoo.flowx.annotation.TagPair;
import org.datayoo.flowx.metadata.FlowNodeMetadata;
import org.datayoo.flowx.node.FlowNode;
import org.datayoo.flowx.node.FlowPort;
import org.datayoo.moql.MoqlException;
import org.datayoo.moql.Operand;
import org.datayoo.sengee.SengeeConstants;
import org.datayoo.sengee.annotation.OpDefiner;
import org.datayoo.sengee.datatype.OperandDataTypeDerivator;
import org.datayoo.sengee.datax.util.ExpressionFactory;
import org.datayoo.sengee.exception.OperationRuntimeException;
import org.datayoo.sengee.op.SengeeOperatorConstants;
import org.datayoo.sengee.op.exception.OperatorsExceptionConstants;
import org.datayoo.sengee.op.processing.AbstractProcessingDescriptor;
import org.datayoo.sengee.op.util.OperatorsI18nMessageResource;
import org.datayoo.sengee.opd.CompilationContext;
import org.datayoo.sengee.opp.OperatorProfileConstants;
import org.datayoo.sengee.util.OpParameterHelper;

import java.util.ArrayList;
import java.util.Map;

@OpDefiner(name = "ValuesSet",
    type = OperatorProfileConstants.OC_PROCESS,
    version = "1.0",
    portrait = "",
    computionFramework = "sengee",
    tags = {
        @TagPair(name = SengeeConstants.TAG_OP_CATS, value = "column,transformer")
    },
    inputPorts = { @Port(name = SengeeOperatorConstants.PORT_DATA_IN,
        flowDataType = SengeeConstants.FDT_ANY,
        option = true)
    },
    outputPorts = { @Port(name = SengeeOperatorConstants.PORT_DATA_OUT,
        flowDataType = SengeeConstants.FDT_ANY,
        option = true)
    },
    parameters =
        "<parameters><parametergroup name=\"general\" c_Alias=\"一般\" c_Compox=\"sightx-tab\">"
            + "<parametertable name=\"columnSet\" c_Alias=\"待赋值列集合\"><head>"
            + "<parameter name=\"columnName\" c_Option=\"false\" c_Alias=\"列名\" c_Compox=\"sengee-column-selector\"></parameter>"
            + "<parameter name=\"colExpr\" c_Alias=\"赋值数据表达式\" c_Option=\"false\"></parameter>"
            + "</head>" + "</parametertable>" + "</parametergroup>"
            + "</parameters>",
    compoxes = {})
public class ValuesSetDescriptor extends AbstractProcessingDescriptor {
  public static final String PARAM_COL_EXPR = "colExpr";

  protected String[][] columnArr;

  public ValuesSetDescriptor(FlowNodeMetadata flowNodeMetadata, FlowNode parent,
      CompilationContext compilationContext) {
    super(flowNodeMetadata, parent, compilationContext);
  }

  @Override
  protected void readParameters() {
    TableParameter columnSetParameter = getColumnSetParameter();
    columnArr = new String[columnSetParameter.getParameters().size()][2];
    int i = 0;
    columns = new ArrayList<>(columnSetParameter.getParameters().size());
    for (RowParameter rowParameter : columnSetParameter.getParameters()) {
      columns.add(rowParameter.getParameterValue(PARAM_COLUMN_NAME));
      columnArr[i][0] = rowParameter.getParameterValue(PARAM_COLUMN_NAME);
      columnArr[i][1] = rowParameter.getParameterValue(PARAM_COL_EXPR);
      i++;
    }
  }

  @Override
  protected ColumnSetMetadata compileCsmOfOutputPort(
      FlowPort<PlRowSet> flowPort) {
    return getInputColumnSetMetadata();
  }

  @Override
  public void validateParameters() {
    super.validateParameters();
    // 校验数据映射配置的数据类型是否准确
    ColumnSetMetadata columnSetMetadata = getInputColumnSetMetadata();
    Map<String, DataType> columnTypeMap = OpParameterHelper.toColumnTypeMap(
        getInputColumnSetMetadata());
    for (String[] param : columnArr) {
      try {
        Operand operand = ExpressionFactory.createExpression(
            param[1]);
        DataType srcType = OperandDataTypeDerivator.derivate(
            operand, columnTypeMap);
        if (!OpParameterHelper.convertable(srcType,
            columnSetMetadata.getColumn(param[0]).getType() )) {
          throw new OperationRuntimeException(OperatorsI18nMessageResource.format(
              OperatorsExceptionConstants.ECM_OPD_UNMATCHED_DATATYPE, this.alias,
              param[0], srcType, columnSetMetadata.getColumn(param[0]).getType()));
        }
      } catch (MoqlException e) {
        throw new OperationRuntimeException(OperatorsI18nMessageResource.format(
            OperatorsExceptionConstants.ECM_PARAM_NOT_VALID_EXPR, this.alias,
            PARAM_COL_EXPR, param[1]), e);
      }
    }
  }
}
