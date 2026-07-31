package org.datayoo.oyez.op.processing.r.join;

import org.datayoo.algox.trie.acda.AcdaHit;
import org.datayoo.algox.trie.acda.AcdaTrie;
import org.datayoo.algox.trie.acda.AcdaTrieBuilder;
import org.datayoo.base.lang.Pair;
import org.datayoo.configx.parameter.GroupParameter;
import org.datayoo.datax.sd.ColumnMetadata;
import org.datayoo.datax.sd.ColumnSetMetadata;
import org.datayoo.datax.sd.GeneralColumnSetMetadata;
import org.datayoo.datax.sd.PlRowSet;
import org.datayoo.flowx.annotation.Port;
import org.datayoo.flowx.metadata.FlowNodeMetadata;
import org.datayoo.flowx.node.FlowNode;
import org.datayoo.oyez.op.EngineContext;
import org.datayoo.sengee.SengeeConstants;
import org.datayoo.sengee.annotation.OpDefiner;
import org.datayoo.sengee.op.processing.r.join.JoinDescriptor;
import org.datayoo.sengee.opp.OperatorProfileConstants;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@OpDefiner(name = "JoinWithRightDict",
    type = OperatorProfileConstants.OC_PROCESS,
    version = "1.0",
    computionFramework = "oyez",
    replica = -1,
    inputPorts = { @Port(name = "leftIn",
        flowDataType = SengeeConstants.FDT_ANY,
        option = false), @Port(name = "rightIn",
        flowDataType = SengeeConstants.FDT_ANY,
        option = false)
    },
    outputPorts = { @Port(name = "dataOut",
        flowDataType = SengeeConstants.FDT_ANY,
        option = false)
    },
    parameters = "",
    compoxes = {})
public class JoinWithRightDict extends BaseSetOperator {

  private String leftColumn;

  private String rightColumn;

  private int leftIndex;

  private int rightIndex;

  protected boolean loadDict = false;

  protected AcdaTrie acdaTrie;

  public JoinWithRightDict(FlowNodeMetadata operatorMetadata, FlowNode parent,
      EngineContext engineContext) {
    super(operatorMetadata, parent, engineContext);
  }

  @Override
  protected void innerOperate() {
    //这块判断必须放在最前边，适配字典定时更新功能
    PlRowSet rightPlRowSet = readAll(rightInput);
    if (rightPlRowSet == null) {
      return;
    }
    rightIndex = rightPlRowSet.getColumnSetMetadata()
        .getColumnIndex(rightColumn);
    Map<String, Pair<String, Object[]>> termMap = loadDict(rightPlRowSet);
    AcdaTrieBuilder<Pair<String, Object[]>> acdaTrieBuilder = new AcdaTrieBuilder<>();
    if (!termMap.isEmpty()) {
      acdaTrie = acdaTrieBuilder.build(termMap);
    }
    PlRowSet leftPlRowSet = leftInput.read();
    leftIndex = leftPlRowSet.getColumnSetMetadata().getColumnIndex(leftColumn);
    PlRowSet outSet = createRowSet(outputColumnSetMetadata);
    for (Object[] objects : leftPlRowSet.getRows()) {
      Object[] out = new Object[outputColumnSetMetadata.getColumns().size()];
      System.arraycopy(objects, 0, out, 0, objects.length);
      String data = (String) objects[leftIndex];
      if (data == null)
        continue;
      if (acdaTrie != null) {
        List<AcdaHit<Pair<String, Object[]>>> acdaHits = acdaTrie.parseText(
            data);
        if (acdaHits.size() > 0) {
          for (AcdaHit<Pair<String, Object[]>> acdaHit : acdaHits) {
            if (acdaHit.value.getKey().length() == data.length()) {
              if (acdaHit.value.getKey().equals(data)) {
                System.arraycopy(acdaHit.value.getValue(), 0, out,
                    objects.length, outputColumnSetMetadata.getColumns().size()
                        - objects.length);
              }
            }
          }
        }
      }
      outSet.addRow(out);
    }
    outputPort.write(outSet, getInputWaterMark());
  }

  @Override
  protected void presetAttributes(GroupParameter groupParameter) {
    leftColumn = parameters.getParameterValue(JoinDescriptor.PARAM_LEFTCOLUMN);
    rightColumn = parameters.getParameterValue(
        JoinDescriptor.PARAM_RIGHTCOLUMN);
  }

  @Override
  protected String buildSetSql(ColumnSetMetadata leftColumnSetMetadata,
      ColumnSetMetadata rightColumnSetMetadata) {
    return null;
  }

  @Override
  protected ColumnSetMetadata buildOutputColumnSetMetadata(
      ColumnSetMetadata leftMetadata, ColumnSetMetadata rightMetadata) {
    Map<String, Integer> columnMap = new HashMap<>();
    List<ColumnMetadata> metadataList = new ArrayList<>();
    //默认单侧不存在重复字段
    for (ColumnMetadata metadata : leftMetadata.getColumns()) {
      columnMap.put(metadata.getName(), 1);
      metadataList.add(metadata);
    }
    for (ColumnMetadata metadata : rightMetadata.getColumns()) {
      if (metadata.getName().equals(rightColumn)) {
        continue;
      } else if (metadataList.contains(metadata)) {
        int count = columnMap.get(metadata.getName());
        metadata.setName(metadata.getName() + count);
        columnMap.put(metadata.getName(), count + 1);
        metadataList.add(metadata);
      } else {
        metadataList.add(metadata);
      }
    }
    ColumnSetMetadata columnSetMetadata = new GeneralColumnSetMetadata("/");
    columnSetMetadata.setColumns(metadataList);
    return columnSetMetadata;
  }

  @Override
  protected boolean isReady() {
    return this.leftInput.isReady();
  }

  protected Map<String, Pair<String, Object[]>> loadDict(
      PlRowSet rightPlRowSet) {
    Map<String, Pair<String, Object[]>> dictMap = new HashMap<>();
    for (Object[] objects : rightPlRowSet.getRows()) {
      Object object = objects[rightIndex];
      if (object == null) {
        continue;
      }
      String key = (String) object;
      Object[] newData = new Object[objects.length - 1];
      int j = 0;
      for (int i = 0; i < objects.length; i++) {
        if (i != rightIndex) {
          newData[j++] = objects[i];
        }
      }
      dictMap.put(key, new Pair<>(key, newData));
    }
    return dictMap;
  }
}
