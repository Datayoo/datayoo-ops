package org.datayoo.sengee.op.processing.st;

import org.datayoo.datax.sd.ColumnSetMetadata;
import org.datayoo.datax.sd.GeneralColumnSetMetadata;
import org.datayoo.datax.sd.PlRowSet;
import org.datayoo.flowx.annotation.Port;
import org.datayoo.flowx.annotation.TagPair;
import org.datayoo.flowx.metadata.FlowNodeMetadata;
import org.datayoo.flowx.node.FlowNode;
import org.datayoo.flowx.node.FlowPort;
import org.datayoo.footstone.sightx.annotation.Compox;
import org.datayoo.footstone.sightx.annotation.DataDesc;
import org.datayoo.footstone.sightx.annotation.VisibleDesc;
import org.datayoo.footstone.sightx.vis.VisibleType;
import org.datayoo.sengee.SengeeConstants;
import org.datayoo.sengee.annotation.OpDefiner;
import org.datayoo.sengee.op.SengeeOperatorConstants;
import org.datayoo.sengee.op.processing.AbstractProcessingDescriptor;
import org.datayoo.sengee.opd.CompilationContext;
import org.datayoo.sengee.opp.OperatorProfileConstants;

/**
 * 行列转换
 *
 * @author hhn
 */
@OpDefiner(name = "COO2Table",
    type = OperatorProfileConstants.OC_PROCESS,
    version = "1.0",
    computionFramework = "sengee",
    tags = {
        @TagPair(name = SengeeConstants.TAG_OP_CATS, value = "rowset,structure")
    },
    inputPorts = { @Port(name = SengeeOperatorConstants.PORT_DATA_IN,
        flowDataType = SengeeConstants.FDT_FLAT_TABLE,
        option = false)
    },
    outputPorts = { @Port(name = SengeeOperatorConstants.PORT_DATA_OUT,
        flowDataType = SengeeConstants.FDT_ANY,
        option = true)
    },
    parameters =
        "<parameters><parametergroup name=\"general\" c_Alias=\"一般\" c_Compox=\"sightx-tab\">"
            + "<parameter name=\"type\" c_Option=\"false\" c_Alias=\"模式\" c_Compox=\"typeCtrl\">0</parameter>"
            + "<parameter name=\"headerType\" c_Option=\"false\" c_Alias=\"列头模式\" c_Compox=\"headerTypeCtrl\">0</parameter>"
            + "<parameter name=\"rowHeaderIndex\" c_Option=\"false\" c_Alias=\"列头所在行\" c_Compox=\"rowHeaderCtrl\">0</parameter>"
            + "<parameter name=\"colHeaderIndex\" c_Option=\"false\" c_Alias=\"列头所在列\" c_Compox=\"colHeaderCtrl\">0</parameter>"
            + "<parameter name=\"rowValueIndex\" c_Option=\"false\" c_Alias=\"值起始行\" c_Compox=\"rowValCtrl\">0</parameter>"
            + "<parameter name=\"colValueIndex\" c_Option=\"false\" c_Alias=\"值起始列\" c_Compox=\"colValCtrl\">0</parameter>"
            + "<parametertable name=\"columnSet\" c_Alias=\"自定义转换\" c_Compox=\"columnSetCtrl\"><head>"
            + "<parameter name=\"colName\" c_Alias=\"头部名称\" c_Option=\"false\"></parameter>"
            + "<parameter name=\"colType\" c_Alias=\"输出列类型\" c_Compox=\"sightx-datatype\" c_Option=\"false\"></parameter>"
            + "<parameter name=\"index\" c_Alias=\"对应数据列序号\" c_Option=\"false\"></parameter>"
            + "</head></parametertable>"
            + "</parametergroup></parameters>",
    compoxes = { @Compox(id = "typeCtrl",
        parentId = "sightx-combox",
        visibleDescriptors = {},
        dataDescriptor = @DataDesc(defaultValue = "RowMode",
            data = "[{\"label\": \"行模式\", \"value\": \"RowMode\"}, {\"label\": \"列模式\", \"value\": \"ColMode\"}]")),
        @Compox(id = "headerTypeCtrl",
            parentId = "sightx-combox",
            visibleDescriptors = {},
            dataDescriptor = @DataDesc(defaultValue = "ValMode",
                data = "[{\"label\": \"值模式\", \"value\": \"ValMode\"}, {\"label\": \"自定义\", \"value\": \"ManMode\"}]")),
        @Compox(id = "rowHeaderCtrl",
            parentId = "sightx-integer",
            visibleDescriptors = {
                @VisibleDesc(visibleType = VisibleType.VISIBLE,
                    condition = "type == \"RowMode\" and headerType == \"ValMode\" ")
            }), @Compox(id = "colHeaderCtrl",
        parentId = "sightx-integer",
        visibleDescriptors = { @VisibleDesc(visibleType = VisibleType.VISIBLE,
            condition = "type == \"ColMode\" and headerType == \"ValMode\"")
        }), @Compox(id = "rowValCtrl",
        parentId = "sightx-integer",
        visibleDescriptors = { @VisibleDesc(visibleType = VisibleType.VISIBLE,
            condition = "type == \"RowMode\" and headerType == \"ValMode\"")
        }), @Compox(id = "colValCtrl",
        parentId = "sightx-integer",
        visibleDescriptors = { @VisibleDesc(visibleType = VisibleType.VISIBLE,
            condition = "type == \"ColMode\" and headerType == \"ValMode\"")
        }), @Compox(id = "columnSetCtrl",
        parentId = "sightx-tablelist",
        visibleDescriptors = { @VisibleDesc(visibleType = VisibleType.VISIBLE,
            condition = "headerType == \"ManMode\"")
        })
    })
public class COO2TableDescriptor extends AbstractProcessingDescriptor {

  public static final String PARAM_TYPE = "type";
  public static final String PARAM_HEADER_TYPE = "headerType";
  public static final String PARAM_ROW_HEADER_INDEX = "rowHeaderIndex";
  public static final String PARAM_COL_HEADER_INDEX = "colHeaderIndex";
  public static final String PARAM_ROW_VAL_INDEX = "rowValueIndex";
  public static final String PARAM_COL_VAL_INDEX = "colValueIndex";
  public static final String PARAM_COL_MAPPINGS = "colMappings";
  public static final String PARAM_COL_SET = "columnSet";
  public static final String PARAM_COL_NAME = "colName";
  public static final String PARAM_COL_TYPE = "colType";
  public static final String PARAM_INDEX = "index";

  public COO2TableDescriptor(FlowNodeMetadata flowNodeMetadata,
      FlowNode parent, CompilationContext compilationContext) {
    super(flowNodeMetadata, parent, compilationContext);
  }

  @Override
  protected ColumnSetMetadata compileCsmOfOutputPort(
      FlowPort<PlRowSet> flowPort) {
    return new GeneralColumnSetMetadata("/");
  }

  @Override
  protected void readParameters() {
  }

  @Override
  public void validateParameters() {
  }

}
