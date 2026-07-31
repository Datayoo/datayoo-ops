package org.datayoo.sengee.op.processing.v.coding;

import org.datayoo.configx.parameter.RowParameter;
import org.datayoo.configx.parameter.TableParameter;
import org.datayoo.flowx.annotation.Port;
import org.datayoo.flowx.annotation.TagPair;
import org.datayoo.flowx.metadata.FlowNodeMetadata;
import org.datayoo.flowx.node.FlowNode;
import org.datayoo.footstone.sightx.annotation.Compox;
import org.datayoo.footstone.sightx.annotation.DataDesc;
import org.datayoo.moql.MoqlException;
import org.datayoo.sengee.SengeeConstants;
import org.datayoo.sengee.annotation.OpDefiner;
import org.datayoo.sengee.datax.util.ExpressionFactory;
import org.datayoo.sengee.op.SengeeOperatorConstants;
import org.datayoo.sengee.op.exception.OperatorsExceptionConstants;
import org.datayoo.sengee.exception.OperationRuntimeException;
import org.datayoo.sengee.op.util.OperatorsI18nMessageResource;
import org.datayoo.sengee.opd.CompilationContext;
import org.datayoo.sengee.opp.OperatorProfileConstants;

@OpDefiner(name = "MdHash",
    type = OperatorProfileConstants.OC_PROCESS,
    version = "1.0",
    computionFramework = "sengee",
    tags = {
        @TagPair(name = SengeeConstants.TAG_OP_CATS, value = "column,coding")
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
            + "<parameter name=\"workingMode\" c_Option=\"false\" c_Alias=\"编码输出模式\" c_Compox=\"workingModeCtrl\"></parameter>"
            + "<parametertable name=\"columnSet\" c_Alias=\"待哈希列集合\"><head>"
            + "<parameter name=\"columnName\" c_Option=\"false\" c_Alias=\"待哈希列\" c_Compox=\"sengee-column-selector\"></parameter>"
            + "<parameter name=\"salt\" c_Option=\"true\" c_Alias=\"盐\" c_Compox=\"sengee-column-input-selector\"></parameter>"
            + "</head></parametertable>"
            + "<parameter name=\"algorithm\" c_Option=\"true\" c_Alias=\"messageDigest算法\" c_Compox=\"algorithmCtrl\"></parameter>"
            + "</parametergroup></parameters>",
    compoxes = { @Compox(id = "algorithmCtrl",
        parentId = "sightx-combox",
        dataDescriptor = @DataDesc(data =
            "[{\"label\": \"MD2\", \"value\": \"MD2\"},"
                + " {\"label\": \"MD5\", \"value\": \"MD5\"},"
                + " {\"label\": \"SHA-1\", \"value\": \"SHA_1\"},"
                + " {\"label\": \"SHA-224\", \"value\": \"SHA_224\"},"
                + " {\"label\": \"SHA-256\", \"value\": \"SHA_256\"},"
                + " {\"label\": \"SHA-384\", \"value\": \"SHA_384\"},"
                + " {\"label\": \"SHA-512\", \"value\": \"SHA_512\"},"
                + " {\"label\": \"SHA-512/224\", \"value\": \"SHA_512_224\"},"
                + " {\"label\": \"SHA-512/256\", \"value\": \"SHA_512_256\"}"
/*                    + " {\"label\": \"SHA3_224\", \"value\": \"SHA3-224\"},"
                    + " {\"label\": \"SHA3_256\", \"value\": \"SHA3-256\"},"
                    + " {\"label\": \"SHA3_384\", \"value\": \"SHA3-384\"},"
                    + " {\"label\": \"SHA3_512\", \"value\": \"SHA3-512\"}" */
                + "]")), @Compox(id = "workingModeCtrl",
        parentId = "sightx-combox",
        visibleDescriptors = {},
        dataDescriptor = @DataDesc(defaultValue = "overwrite",
            data = "[{\"label\": \"列值覆盖\", \"value\": \"overwrite\"}, {\"label\": \"新增列\", \"value\": \"addColumn\"}]"))

    })
public class MdHashDescriptor extends AbstractCodecDescriptor {

  public static final String PARAM_MD_ALGORITHM = "algorithm";

  public static final String COL_SUFFIX = "md";
  public static final String PARAM_SALT = "salt";

  public MdHashDescriptor(FlowNodeMetadata flowNodeMetadata, FlowNode parent,
      CompilationContext compilationContext) {
    super(flowNodeMetadata, parent, compilationContext);
  }

  @Override
  protected String getColumnSuffix() {
    return COL_SUFFIX;
  }

  protected void validateParameters() {
    super.validateParameters();
    MessageDigestAlgorithm digestAlgorithm = MessageDigestAlgorithm.valueOf(
        parameters.getParameterValue(PARAM_MD_ALGORITHM,
            MessageDigestAlgorithm.MD5.name()));
    TableParameter columnSetParameter = (TableParameter) parameters.getParameter(
        AesDecodeDescriptor.PARAM_COLUMN_SET);
    for (RowParameter rowParameter : columnSetParameter.getParameters()) {
      String salt = rowParameter.getParameterValue(PARAM_SALT, "");
      if (!salt.isEmpty()) {
        try {
          ExpressionFactory.createExpression(salt);
        } catch (MoqlException e) {
          throw new OperationRuntimeException(
              OperatorsI18nMessageResource.format(
                  OperatorsExceptionConstants.ECM_PARAM_NOT_VALID_EXPR, this.alias,
                  PARAM_SALT, salt), e);
        }
      }
    }
  }

  public static enum MessageDigestAlgorithm {
    MD2("MD2"),
    MD5("MD5"),
    SHA_1("SHA-1"),
    SHA_224("SHA-224"),
    SHA_256("SHA-256"),
    SHA_384("SHA-384"),
    SHA_512("SHA-512"),
    SHA_512_224("SHA-512/224"),
    SHA_512_256("SHA-512/256");
    private String algorName;

    private MessageDigestAlgorithm(String name) {
      this.algorName = name;
    }

    public String getAlgorName() {
      return algorName;
    }

    public static MessageDigestAlgorithm findAlgorithmByName(String algorName) {
      if (MD2.getAlgorName().equals(algorName))
        return MD2;
      if (MD5.getAlgorName().equals(algorName))
        return MD5;
      if (SHA_1.getAlgorName().equals(algorName))
        return SHA_1;
      if (SHA_224.getAlgorName().equals(algorName))
        return SHA_224;
      if (SHA_256.getAlgorName().equals(algorName))
        return SHA_256;
      if (SHA_384.getAlgorName().equals(algorName))
        return SHA_384;
      if (SHA_512.getAlgorName().equals(algorName))
        return SHA_512;
      if (SHA_512_224.getAlgorName().equals(algorName))
        return SHA_512_224;
      if (SHA_512_256.getAlgorName().equals(algorName))
        return SHA_512_256;
      throw new IllegalArgumentException(
          String.format("Unsupported algorithm '%s'!", algorName));
    }
  }
}
