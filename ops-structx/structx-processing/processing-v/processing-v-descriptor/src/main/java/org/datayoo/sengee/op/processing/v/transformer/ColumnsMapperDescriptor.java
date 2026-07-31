package org.datayoo.sengee.op.processing.v.transformer;

import org.apache.commons.lang3.StringUtils;
import org.datayoo.base.types.DataType;
import org.datayoo.datax.sd.ColumnMetadata;
import org.datayoo.datax.sd.ColumnSetMetadata;
import org.datayoo.datax.sd.GeneralColumnSetMetadata;
import org.datayoo.datax.sd.PlRowSet;
import org.datayoo.flowx.annotation.Port;
import org.datayoo.flowx.annotation.TagPair;
import org.datayoo.flowx.metadata.FlowNodeMetadata;
import org.datayoo.flowx.node.FlowConnection;
import org.datayoo.flowx.node.FlowNode;
import org.datayoo.flowx.node.FlowPort;
import org.datayoo.sengee.SengeeConstants;
import org.datayoo.sengee.annotation.OpDefiner;
import org.datayoo.sengee.datax.mapper.ColumnMappingEntry;
import org.datayoo.sengee.datax.mapper.ColumnsMapper;
import org.datayoo.sengee.exception.OperationRuntimeException;
import org.datayoo.sengee.op.SengeeOperatorConstants;
import org.datayoo.sengee.op.exception.OperatorsExceptionConstants;
import org.datayoo.sengee.op.processing.AbstractProcessingDescriptor;
import org.datayoo.sengee.op.util.OperatorsI18nMessageResource;
import org.datayoo.sengee.opd.CompilationContext;
import org.datayoo.sengee.opd.OperatorDescriptor;
import org.datayoo.sengee.opp.OperatorProfileConstants;
import org.datayoo.sengee.util.OpParameterHelper;

import java.util.*;

/**
 * @author tangtadin
 * @version 1.0
 * @description: TODO
 * @date 2021/7/13 4:12 PM
 */
@OpDefiner(name = "ColumnsMapper",
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
    parameters = "<parameters>"
        + "    <parametergroup name=\"columnSet\" c_Alias=\"列集\" c_Compox=\"sightx-tab\">"
        + "        <parametergroup name=\"colMappings\" c_Alias=\"列集合\" c_Compox=\"sengee-colMappings\">"
        + "            <parametergroup name=\"mappingEntry\">"
        + "                <parameter name=\"colName\" c_Alias=\"输出列名称\" c_Option=\"false\"></parameter>"
        + "                <parameter name=\"colType\" c_Alias=\"输出列类型\" c_Compox=\"sightx-datatype\" c_Option=\"false\"></parameter>"
        + "                <parameter name=\"colExpr\" c_Alias=\"输出数据表达式\" c_Option=\"false\"></parameter>"
        + "            </parametergroup>" + "        </parametergroup>"
        + "    </parametergroup>" + "</parameters>",
    compoxes = {})
public class ColumnsMapperDescriptor extends AbstractProcessingDescriptor {

  protected List<ColumnMappingEntry> columnMappingEntries;

  public ColumnsMapperDescriptor(FlowNodeMetadata flowNodeMetadata,
      FlowNode parent, CompilationContext compilationContext) {
    super(flowNodeMetadata, parent, compilationContext);
  }

  @Override
  protected ColumnSetMetadata compileCsmOfOutputPort(
      FlowPort<PlRowSet> flowPort) {
    GeneralColumnSetMetadata outColumnSetMetadata = new GeneralColumnSetMetadata(
        flowPort.getFlowDataType());
    for (ColumnMappingEntry mappingEntry : columnMappingEntries) {
      if (StringUtils.isEmpty(mappingEntry.getName()))
        continue;
      outColumnSetMetadata.addColumn(mappingEntry.toColumnMetadata());
    }
    return outColumnSetMetadata;
  }

  @Override
  protected void readParameters() {
    Map<String, DataType> colTypes = buildColTypes();
    ColumnsMapper columnsMapper = new ColumnsMapper(parameters, colTypes);
    columnMappingEntries = columnsMapper.getColumnMappingEntries();
  }

  protected Map<String, DataType> buildColTypes() {
    FlowPort flowPort = this.inputPorts.iterator().next();
    Set<FlowConnection> flowConnections = flowPort.getFromConnections();
    if (flowConnections.size() > 0) {
      FlowConnection flowConnection = flowConnections.iterator().next();
      OperatorDescriptor operatorDescriptor = (OperatorDescriptor) flowConnection.getSourcePort()
          .getOwner();
      ColumnSetMetadata columnSetMetadata = operatorDescriptor.getPortColumnSetMetadata(
          flowConnection.getSourcePort().getName());
      Map<String, DataType> colTypes = new HashMap<>();
      for (ColumnMetadata columnMetadata : columnSetMetadata.getColumns()) {
        colTypes.put(columnMetadata.getName(), columnMetadata.getType());
      }
      return colTypes;
    }
    return null;
  }

  protected void validateParameters() {
    if (columnMappingEntries.size() == 0) {
      throw new OperationRuntimeException(OperatorsI18nMessageResource.format(
          OperatorsExceptionConstants.ECM_PARAM_EMPTY, this.alias,
          ColumnsMapper.PARAM_COL_MAPPINGS));
    }
    // 校验数据映射配置的数据类型是否准确
    //validateDataType(getInputColumnSetMetadata(), columnMappingEntries);
  }

  protected void validateDataType(ColumnSetMetadata columnSetMetadata,
      List<ColumnMappingEntry> columnMappingEntries) {
    Map<String, DataType> columnTypeMap = OpParameterHelper.toColumnTypeMap(
        columnSetMetadata);
    Set<String> columnSet = new HashSet<>();
    for (ColumnMappingEntry mappingEntry : columnMappingEntries) {
      mappingEntry.validateDataType(columnTypeMap);
      if (!columnSet.add(mappingEntry.getName())) {
        throw new OperationRuntimeException(OperatorsI18nMessageResource.format(
            OperatorsExceptionConstants.ECM_RESOURCE_DUPLICATED, this.alias,
            mappingEntry.getName()));
      }
      if (mappingEntry.getChildMappingEntries() != null) {
        if (mappingEntry.getChildMappingEntries().size() > 0) {
          validateDataType(columnSetMetadata,
              mappingEntry.getChildMappingEntries());
        }
      }
    }
  }

}
