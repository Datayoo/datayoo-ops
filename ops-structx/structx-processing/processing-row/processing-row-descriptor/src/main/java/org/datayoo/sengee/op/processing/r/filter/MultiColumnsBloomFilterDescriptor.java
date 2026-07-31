package org.datayoo.sengee.op.processing.r.filter;

import org.datayoo.base.types.DataType;
import org.datayoo.base.types.DataTypeName;
import org.datayoo.configx.parameter.RowParameter;
import org.datayoo.configx.parameter.TableParameter;
import org.datayoo.datax.sd.ColumnSetMetadata;
import org.datayoo.datax.sd.GeneralColumnSetMetadata;
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

/**
 * 多字段bloom过滤
 *
 * @author hhn
 */
@OpDefiner(name = "MultiColumnsBloomFilter",
    type = OperatorProfileConstants.OC_PROCESS,
    version = "1.0",
    computionFramework = "sengee",
    tags = {
        @TagPair(name = SengeeConstants.TAG_OP_CATS, value = "rowset,filter")
    },
    inputPorts = { @Port(name = SengeeOperatorConstants.PORT_RESOURCE_IN,
        flowDataType = SengeeConstants.FDT_ANY,
        option = false), @Port(name = SengeeOperatorConstants.PORT_DATA_IN,
        flowDataType = SengeeConstants.FDT_ANY,
        option = false)
    },
    outputPorts = { @Port(name = SengeeOperatorConstants.PORT_DATA_OUT,
        flowDataType = SengeeConstants.FDT_ANY,
        option = true)
    },
    parameters =
        "<parameters><parametergroup name=\"general\" c_Alias=\"一般\" c_Compox=\"sightx-tab\">"
            + "<parametertable name=\"columnSet\" c_Alias=\"待保留列集合\"><head>"
            + "<parameter name=\"srcColumn\" c_Option=\"false\" c_Alias=\"字典数据集\" c_Compox=\"sengee-resIn:all-column-selector\"></parameter>"
            + "<parameter name=\"column\" c_Option=\"false\" c_Alias=\"待过滤数据列\" c_Compox=\"sengee-dataIn:all-column-selector\"></parameter>"
            + "</head>" + "</parametertable>"
            + "<parameter name=\"fpp\" c_Option=\"false\" c_Alias=\"误差率\" c_Compox=\"sightx-double\">0.03</parameter>"
            + "<parameter name=\"charset\" c_Alias=\"字符集\" c_Compox=\"sightx-file-charset\">UTF-8</parameter>"
            + "</parametergroup></parameters>",
    compoxes = {})
public class MultiColumnsBloomFilterDescriptor
    extends AbstractProcessingDescriptor {

  public static final String PARAM_SRC_COLUMN = "srcColumn";
  public static final String PARAM_COLUMN = "column";
  public static final String PARAM_FPP = "fpp";
  public static final String PARAM_CHARSET = "charset";

  public MultiColumnsBloomFilterDescriptor(FlowNodeMetadata flowNodeMetadata,
      FlowNode parent, CompilationContext compilationContext) {
    super(flowNodeMetadata, parent, compilationContext);
  }

  @Override
  protected ColumnSetMetadata compileCsmOfOutputPort(
      FlowPort<PlRowSet> flowPort) {
    ColumnSetMetadata columnSetMetadata = (ColumnSetMetadata) this.columnSetMetadataMap.get(
        SengeeOperatorConstants.PORT_DATA_IN);
    if (columnSetMetadata != null) {
      return new GeneralColumnSetMetadata(columnSetMetadata);
    }
    return null;
  }

  @Override
  protected void readParameters() {
  }

  @Override
  public void validateParameters() {
    ColumnSetMetadata srcMetadata = getPortColumnSetMetadata(
        SengeeOperatorConstants.PORT_RESOURCE_IN);
    ColumnSetMetadata dstMetadata = getPortColumnSetMetadata(
        SengeeOperatorConstants.PORT_DATA_IN);
    TableParameter columnSetParameter = this.getColumnSetParameter();
    for (RowParameter rowParameter : columnSetParameter.getParameters()) {
      String srcColumn = rowParameter.getParameterValue(PARAM_SRC_COLUMN);
      String dataColumn = rowParameter.getParameterValue(PARAM_COLUMN);
      DataType srcType = srcMetadata.getColumn(srcColumn).getType();
      if (!srcType.getName().equals(DataTypeName.Integer) && !srcType.getName()
          .equals(DataTypeName.String) && srcType.getName()
          .equals(DataTypeName.Long)) {
        throw new OperationRuntimeException(OperatorsI18nMessageResource.format(
            OperatorsExceptionConstants.ECM_OPD_COL_INVALID_TYPE, this.alias,
            this.parameters.getParameterValue(PARAM_SRC_COLUMN),
            "String,Long,Integer"));
      }
      DataType dstType = dstMetadata.getColumn(dataColumn).getType();
      if (!srcType.equals(dstType)) {
        throw new OperationRuntimeException(OperatorsI18nMessageResource.format(
            OperatorsExceptionConstants.ECM_OPD_UNMATCHED_DATATYPE, this.alias,
            this.parameters.getParameterValue(PARAM_SRC_COLUMN),
            this.parameters.getParameterValue(PARAM_COLUMN)));
      }
    }
  }

}
