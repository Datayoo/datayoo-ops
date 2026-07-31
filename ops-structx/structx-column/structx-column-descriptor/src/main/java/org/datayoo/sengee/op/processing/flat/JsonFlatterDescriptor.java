package org.datayoo.sengee.op.processing.flat;

import com.google.gson.*;
import org.datayoo.base.lang.Pair;
import org.datayoo.base.types.DataTypeName;
import org.datayoo.configx.parameter.GroupParameter;
import org.datayoo.configx.parameter.util.ParameterUtils;
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
import org.datayoo.sengee.datax.flatter.StructureCollectionMetadata;
import org.datayoo.sengee.datax.flatter.StructureFieldMetadata;
import org.datayoo.sengee.op.SengeeOperatorConstants;
import org.datayoo.sengee.exception.OperationRuntimeException;
import org.datayoo.sengee.opd.AutoStructMappingDescriptor;
import org.datayoo.sengee.opd.CompilationContext;
import org.datayoo.sengee.opp.OperatorProfileConstants;
import org.datayoo.util.xml.XmlAccessException;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

@OpDefiner(name = "JsonFlatter",
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
            + "<parameter name=\"dataColumn\" c_Alias=\"Json数据列\" c_Compox=\"sengee-string-column-selector\"></parameter>"
            + "<parametergroup name=\"structureMappings\" c_Alias=\"结构映射\" c_Compox=\"sengee-self-structureMappings\"  c_AutoMapping=\"true\">"
            + "<parameter name=\"dataExample\" c_Alias=\"数据样例\" c_Compox=\"sengee-json-sample\"></parameter>"
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
public class JsonFlatterDescriptor extends AbstractFlatterDescriptor
    implements AutoStructMappingDescriptor {

  public JsonFlatterDescriptor(FlowNodeMetadata flowNodeMetadata,
      FlowNode parent, CompilationContext compilationContext) {
    super(flowNodeMetadata, parent, compilationContext);
  }

  @Override
  public String autoMapping(String structXml, String dataPath, String data) {
    if (data == null || data.isEmpty())
      return "";
    StructureCollectionMetadata structureCollectionMetadata = loadStructure(
        structXml);
    JsonParser jsonParser = new JsonParser();
    JsonElement jsonElement = jsonParser.parseString(data);
    Pair<String, String> levelLocation = locationLevel(
        structureCollectionMetadata, dataPath);
    if (levelLocation == null)
      return "";
    String[] pathSegs = levelLocation.getValue().split("\\.");
    JsonElement je = locate(pathSegs, 1, jsonElement);
    if (je == null)
      return "";
    return mapping(levelLocation.getKey(), je);
  }

  protected StructureCollectionMetadata loadStructure(String structXml) {
    try {
      GroupParameter groupParameter = ParameterUtils.fromXmlString2Group(
          structXml);
      return loadStructureMappings(groupParameter);
    } catch (XmlAccessException e) {
      throw new OperationRuntimeException(e);
    }
  }

  protected String mapping(String fieldName, JsonElement je) {
    if (je instanceof JsonPrimitive)
      return null;
    JsonObject jo = null;
    if (je instanceof JsonArray) {
      JsonArray ja = (JsonArray) je;
      JsonElement cje = ja.get(0);
      if (!(cje instanceof JsonObject))
        return null;
      jo = (JsonObject) cje;
    } else {
      jo = (JsonObject) je;
    }
    List<StructureFieldMetadata> fieldMetadatas = mapping(fieldName, jo);
    Gson gson = new Gson();
    return gson.toJson(fieldMetadatas);
  }

  protected List<StructureFieldMetadata> mapping(String fieldName,
      JsonObject jo) {
    List<StructureFieldMetadata> fieldMetadatas = new LinkedList();
    for (Map.Entry<String, JsonElement> entry : jo.entrySet()) {
      StructureFieldMetadata fieldMetadata = new StructureCollectionMetadata();
      fieldMetadata.setFieldName(entry.getKey());
      fieldMetadata.setDataPath(
          String.format("%s.%s", fieldName, entry.getKey()));
      if (entry.getValue() instanceof JsonPrimitive) {
        fieldMetadata.setFieldType(
            getDataType((JsonPrimitive) entry.getValue()));
      } else {
        fieldMetadata.setFieldType(DataTypeName.String);
        fieldMetadata.setFormatFunc(
            String.format("json2Object(%s)", entry.getKey()));
      }
      fieldMetadatas.add(fieldMetadata);
    }
    return fieldMetadatas;
  }

  protected DataTypeName getDataType(JsonPrimitive jp) {
    if (jp.isBoolean())
      return DataTypeName.Boolean;
    if (jp.isNumber()) {
      Number number = jp.getAsNumber();
      if (number instanceof Float)
        return DataTypeName.Float;
      if (number instanceof Double)
        return DataTypeName.Double;
      if (number instanceof Integer)
        return DataTypeName.Integer;
      if (number instanceof Long)
        return DataTypeName.Long;
    }
    return DataTypeName.String;
  }

  protected JsonElement locate(String[] pathSegs, int level,
      JsonElement jsonElement) {
    if (pathSegs.length == level)
      return jsonElement;
    JsonObject jo = null;
    if (jsonElement instanceof JsonArray) {
      JsonArray ja = (JsonArray) jsonElement;
      JsonElement je = ja.get(0);
      if (!(je instanceof JsonObject))
        return je;
      jo = (JsonObject) je;
    } else if (jsonElement instanceof JsonObject) {
      jo = (JsonObject) jsonElement;
    } else {
      return jsonElement;
    }
    JsonElement childElement = jo.get(pathSegs[level]);
    return locate(pathSegs, level + 1, childElement);
  }

  protected Pair<String, String> locationLevel(
      StructureCollectionMetadata structureCollectionMetadata,
      String dataPath) {
    String[] segs = dataPath.split("\\.");
    if (segs.length == 1) {
      return new Pair<>(structureCollectionMetadata.getFieldName(),
          structureCollectionMetadata.getDataPath());
    }
    StringBuilder sbud = new StringBuilder();
    sbud.append(segs[segs.length - 1]);
    for (int i = 0; i < segs.length - 1; i++) {
      structureCollectionMetadata = locationLevel(structureCollectionMetadata,
          segs[i], sbud);
    }
    if (structureCollectionMetadata == null
        || structureCollectionMetadata.getChildCollectionMetadata() == null)
      return null;
    return new Pair<>(
        structureCollectionMetadata.getChildCollectionMetadata().getFieldName(),
        sbud.toString());
  }

  protected StructureCollectionMetadata locationLevel(
      StructureCollectionMetadata parent, String levelName,
      StringBuilder abpBud) {
    if (parent == null)
      return null;
    String dataPath = parent.getDataPath();
    int inx = dataPath.indexOf(".");
    if (inx != -1) {
      dataPath = dataPath.substring(inx + 1);
    }
    if (abpBud.length() == 0) {
      abpBud.insert(0, dataPath);
    } else {
      abpBud.insert(0, dataPath + ".");
    }
    if (parent.getFieldName().equals(levelName)) {
      return parent;
    }
    return locationLevel(parent.getChildCollectionMetadata(), levelName,
        abpBud);
  }
}
