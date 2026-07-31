package org.datayoo.sengee.op.processing.r;

import org.datayoo.configx.parameter.GroupParameter;
import org.datayoo.datax.sd.ColumnSetMetadata;
import org.datayoo.datax.sd.GeneralColumnSetMetadata;
import org.datayoo.datax.sd.PlRowSet;
import org.datayoo.datax.sd.TableEntry;
import org.datayoo.flowx.annotation.Port;
import org.datayoo.flowx.annotation.TagPair;
import org.datayoo.flowx.metadata.FlowNodeMetadata;
import org.datayoo.flowx.node.FlowNode;
import org.datayoo.flowx.node.FlowPort;
import org.datayoo.moql.MoqlException;
import org.datayoo.moql.metadata.QueryableMetadata;
import org.datayoo.moql.metadata.SelectorMetadata;
import org.datayoo.moql.metadata.TableMetadata;
import org.datayoo.moql.parser.MoqlParser;
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
import org.datayoo.sengee.opd.TableListDescriptor;
import org.datayoo.sengee.opp.OperatorProfileConstants;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @创建人 hhn
 * @备注
 * 因sql解析后无法获知字段对应数据类型
 * 故采用手动设置的形式
 **/
@OpDefiner(name = "SparkSQL",
    type = OperatorProfileConstants.OC_PROCESS,
    version = "1.0",
    portrait = "",
    computionFramework = "sengee",
    tags = { @TagPair(name = SengeeConstants.TAG_OP_CATS, value = "rowset,sql")},
    inputPorts = { @Port(name = SengeeOperatorConstants.PORT_DATA_IN,
        connectionLimit = 1,
        flowDataType = SengeeConstants.FDT_ANY)
    },
    outputPorts = { @Port(name = SengeeOperatorConstants.PORT_DATA_OUT,
        flowDataType = SengeeConstants.FDT_ANY,
        option = true)
    },
    parameters =
        "<parameters><parametergroup name=\"general\" c_Alias=\"一般\" c_Compox=\"sightx-tab\">"
            + "<parameter name=\"sql\" c_Alias=\"SQL\" c_Option=\"false\" c_Compox=\"sengee-sql\"></parameter>"
            + "<parametergroup name=\"columnSet\" c_Alias=\"列集\">"
            + "    <parametergroup name=\"colMappings\" c_Alias=\"列集合\" c_Compox=\"sengee-colMappings\">"
            + "          <parametergroup name=\"mappingEntry\">"
            + "                <parameter name=\"colName\" c_Alias=\"输出列名称\"></parameter>"
            + "                <parameter name=\"colType\" c_Alias=\"输出列类型\" c_Compox=\"sightx-datatype\">String</parameter>"
            + "                <parameter name=\"colExpr\" c_Alias=\"输出数据表达式\"></parameter>"
            + "           </parametergroup>"
            + "</parametergroup>"
            + "</parametergroup></parametergroup></parameters>",
    compoxes = {})
public class SparkSQLDescriptor extends AbstractProcessingDescriptor
    implements TableListDescriptor {
  public final static String PARAM_SPARK_SQL = "sql";
  protected String sql;
  protected String tableName = "tempView";
  protected List<org.datayoo.datax.sd.ColumnMetadata> columnSet;
  protected List<ColumnMappingEntry> columnMappingEntries;

  public SparkSQLDescriptor(FlowNodeMetadata flowNodeMetadata, FlowNode parent,
      CompilationContext compilationContext) {
    super(flowNodeMetadata, parent, compilationContext);
  }

  @Override
  protected void readParameters() {
    sql = parameters.getParameterValue(PARAM_SPARK_SQL);
    ColumnsMapper columnsMapper = new ColumnsMapper(
        (GroupParameter) parameters.getParameter(
            AbstractProcessingDescriptor.PARAM_COLUMN_SET));
    columnMappingEntries = columnsMapper.getColumnMappingEntries();
  }

  @Override
  protected ColumnSetMetadata compileCsmOfOutputPort(
      FlowPort<PlRowSet> flowPort) {
    GeneralColumnSetMetadata outColumnSetMetadata = new GeneralColumnSetMetadata(
        flowPort.getFlowDataType());
    for (ColumnMappingEntry mappingEntry : columnMappingEntries) {
      outColumnSetMetadata.getColumns().add(mappingEntry.toColumnMetadata());
    }
    return outColumnSetMetadata;
  }

  @Override
  public void validateParameters() {
    if (sql.isEmpty()) {
      throw new OperationRuntimeException(OperatorsI18nMessageResource.format(
          OperatorsExceptionConstants.ECM_PARAM_EMPTY, this.alias,
          PARAM_SPARK_SQL));
    }
    try {
      SelectorMetadata selectorMetadata = (SelectorMetadata) MoqlParser.parseMoql(
          sql);
      List<QueryableMetadata> tables = selectorMetadata.getTables().getTables();
      //sql中不能包含多余一个表名
      if (tables.size() > 1) {
        throw new OperationRuntimeException(OperatorsI18nMessageResource.format(
            OperatorsExceptionConstants.ECM_PARAM_NOT_VALID_EXPR, this.alias,
            PARAM_SPARK_SQL, sql));
      }
      TableMetadata table = (TableMetadata) tables.get(0);
      if (!table.getValue().equals(tableName)) {
        throw new OperationRuntimeException(OperatorsI18nMessageResource.format(
            OperatorsExceptionConstants.ECM_PARAM_NOT_VALID_EXPR, this.alias,
            PARAM_SPARK_SQL, sql));
      }
    } catch (MoqlException e) {
      throw new OperationRuntimeException(OperatorsI18nMessageResource.format(
            OperatorsExceptionConstants.ECM_PARAM_NOT_VALID_EXPR, this.alias,
            PARAM_SPARK_SQL, sql), e);
    }
  }

  @Override
  public List<String> listCatalogs(String s) {
    return null;
  }

  @Override
  public List<String> listSchemas(String catalog,String pattern) {
    return Collections.emptyList();
  }

  @Override
  public List<TableEntry> listTables(String catalog,String s, String pattern) {
    List<TableEntry> tableEntries = new ArrayList<>();
    FlowPort flowPort = this.inputPorts.iterator().next();
    if (flowPort.getFromPorts().size() > 0) {
      TableEntry tableEntry = new TableEntry();
      tableEntry.setName(tableName);
      tableEntries.add(tableEntry);
    }
    return tableEntries;
  }

  @Override
  public ColumnSetMetadata getColumnSetMetadata(String catalog,String s, String s1) {
    OperatorDescriptor operatorDescriptor = (OperatorDescriptor) getParent(this.getParent());
    operatorDescriptor.compileColumnSetMetadata();
    return getInputColumnSetMetadata();
  }

  protected FlowNode getParent(FlowNode flowNode){
    FlowNode parent = flowNode.getParent();
    if(parent == null){
      return flowNode;
    }else{
      return getParent(parent);
    }
  }

}