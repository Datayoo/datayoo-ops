package org.datayoo.sengee.op.processing.flat;

import org.datayoo.flowx.annotation.Port;
import org.datayoo.flowx.annotation.TagPair;
import org.datayoo.flowx.metadata.FlowNodeMetadata;
import org.datayoo.flowx.node.FlowNode;
import org.datayoo.footstone.sightx.annotation.Compox;
import org.datayoo.footstone.sightx.annotation.DataDesc;
import org.datayoo.footstone.sightx.annotation.VisibleDesc;
import org.datayoo.footstone.sightx.vis.VisibleType;
import org.datayoo.sengee.SengeeConstants;
import org.datayoo.sengee.annotation.OpDefiner;
import org.datayoo.sengee.op.SengeeOperatorConstants;
import org.datayoo.sengee.opd.CompilationContext;
import org.datayoo.sengee.opp.OperatorProfileConstants;

@OpDefiner(name = "XmlFlatter",
    type = OperatorProfileConstants.OC_PROCESS,
    version = "1.0",
    portrait = "",
    computionFramework = "sengee",
    tags = { @TagPair(name = SengeeConstants.TAG_OP_CATS, value = "rowset,flat")
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
            + "<parameter name=\"dataColumn\" c_Alias=\"Xml数据列\" c_Compox=\"sengee-string-column-selector\"></parameter>"
            + "<parametergroup name=\"structureMappings\" c_Alias=\"结构映射\" c_Compox=\"sengee-self-structureMappings\">"
            + "<parameter name=\"dataExample\" c_Alias=\"数据样例\" c_Compox=\"sengee-xml-sample\"></parameter>"
            + "<parameter name=\"mappingName\" c_Alias=\"字段映射名\">o</parameter>"
            + "<parameter name=\"dataPath\" c_Alias=\"结构字段路径\">o</parameter>"
            + "<parameter name=\"filterMode\" c_Alias=\"过滤模式\" c_Compox=\"filterModeCtrl\">filter</parameter>"
            + "<parameter name=\"startPos\" c_Alias=\"起始位置\" c_Compox=\"startPosCtrl\">0</parameter>"
            + "<parameter name=\"endPos\" c_Alias=\"结束位置\" c_Compox=\"endPosCtrl\">-1</parameter>"
            + "<parameter name=\"filter\" c_Alias=\"过滤条件\" c_Compox=\"filterCtrl\"></parameter>"
            + "<parametertable name=\"structureFields\" c_Alias=\"结构字段映射\">"
            + "<head><parameter name=\"columnName\" c_Alias=\"输出列名\"></parameter>"
            + "<parameter name=\"dataPath\" c_Alias=\"结构字段路径\"></parameter>"
            + "<parameter name=\"columnType\" c_Alias=\"输出列数据类型\" c_Compox=\"sightx-datatype\"></parameter>"
            + "<parameter name=\"formatFunc\" c_Alias=\"数据转换函数\" c_Option=\"true\"></parameter>"
            + "</head>" + "</parametertable>"
            + "</parametergroup></parametergroup></parameters>",
    compoxes = { @Compox(id = "filterModeCtrl",
        parentId = "sightx-switch",
        visibleDescriptors = {},
        dataDescriptor = @DataDesc(defaultValue = "filter",
            data = "[{\"label\": \"过滤\", \"value\": \"filter\"}, {\"label\": \"索引\", \"value\": \"index\"}]")),
        @Compox(id = "startPosCtrl",
            parentId = "sightx-uinteger",
            visibleDescriptors = {
                @VisibleDesc(visibleType = VisibleType.VISIBLE,
                    condition = "filterMode == \"index\"")
            }), @Compox(id = "endPosCtrl",
        parentId = "sightx-integer",
        visibleDescriptors = { @VisibleDesc(visibleType = VisibleType.VISIBLE,
            condition = "filterMode == \"index\"")
        }), @Compox(id = "filterCtrl",
        parentId = "sightx-input",
        visibleDescriptors = { @VisibleDesc(visibleType = VisibleType.VISIBLE,
            condition = "filterMode == \"filter\"")
        })
    })
public class XmlFlatterDescriptor extends AbstractFlatterDescriptor {

  public XmlFlatterDescriptor(FlowNodeMetadata flowNodeMetadata,
      FlowNode parent, CompilationContext compilationContext) {
    super(flowNodeMetadata, parent, compilationContext);
  }

}
