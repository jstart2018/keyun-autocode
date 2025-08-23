package com.jstart.keyunautocodebackend.core;


import cn.hutool.json.JSONUtil;
import com.jstart.keyunautocodebackend.ai.AiCodeGeneratorService;
import com.jstart.keyunautocodebackend.ai.AiCodeGeneratorServiceFactory;
import com.jstart.keyunautocodebackend.ai.model.message.AiResponseMessage;
import com.jstart.keyunautocodebackend.ai.model.message.ToolExecutedMessage;
import com.jstart.keyunautocodebackend.ai.model.message.ToolRequestMessage;
import com.jstart.keyunautocodebackend.constant.AppConstant;
import com.jstart.keyunautocodebackend.core.codeParser.CodeParserExecutor;
import com.jstart.keyunautocodebackend.core.fileSaver.FileSaveExecutor;
import com.jstart.keyunautocodebackend.core.projectBuilder.VueProjectBuilder;
import com.jstart.keyunautocodebackend.enums.CodeGenTypeEnum;
import com.jstart.keyunautocodebackend.exception.BusinessException;
import com.jstart.keyunautocodebackend.model.ResultEnum;
import com.jstart.keyunautocodebackend.model.entity.App;
import com.jstart.keyunautocodebackend.service.AppService;
import dev.langchain4j.model.chat.response.ChatResponse;
import dev.langchain4j.service.TokenStream;
import dev.langchain4j.service.tool.ToolExecution;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.io.File;

/**
 * AI代码生成器 门面类，组合代码生成和保存功能
 */
@Service
@Slf4j
public class AiCodeGeneratorFacade {

    @Resource
    private AiCodeGeneratorServiceFactory aiCodeGeneratorServiceFactory;
    @Resource
    private VueProjectBuilder vueProjectBuilder;


    /**
     * 统一入口：根据类型生成并保存代码（流式）
     *
     * @param userMessage     用户提示词
     * @param codeGenTypeEnum 生成类型
     */
    public Flux<String> generateAndSaveCodeStream(String userMessage, CodeGenTypeEnum codeGenTypeEnum, App app) {
        if (codeGenTypeEnum == null) {
            throw new BusinessException(ResultEnum.SYSTEM_ERROR, "生成类型为空");
        }
        //获取 AI 代码生成服务实例
        Long appId = app.getId();
        AiCodeGeneratorService aiCodeGeneratorService = aiCodeGeneratorServiceFactory.getAiCodeGeneratorService(appId, codeGenTypeEnum);
        log.info("获取 AI 代码生成服务实例: {}", aiCodeGeneratorService.getClass().getSimpleName());

        return switch (codeGenTypeEnum) {
            case HTML -> {
                //润色用户首次输入，减少用户等待和成本
                if (app.getInitPrompt().equals(userMessage)){
                    userMessage = userMessage + "。代码不超过20行！！";
                }
                Flux<String> result = aiCodeGeneratorService.generateHtmlCodeStream(userMessage);
                yield parserAndSaveResult(result, codeGenTypeEnum, appId);
            }
            case MULTI_FILE -> {
                //润色用户首次输入，减少用户等待和成本
                if (app.getInitPrompt().equals(userMessage)){
                    userMessage = userMessage + "。代码不超过50行！！";
                }
                Flux<String> result = aiCodeGeneratorService.generateMultiFileCodeStream(userMessage);
                yield parserAndSaveResult(result, codeGenTypeEnum, appId);
            }
            case VUE_PROJECT -> {
                //润色用户首次输入，减少用户等待和成本
                if (app.getInitPrompt().equals(userMessage)){
                    userMessage = userMessage + "。去掉其中你觉得需要大量编码的功能，保留其中最多三个功能，页面不得超过三个，代码不超过90行！！！而且千万不能输出关于核心代码有多少行、以及删除了哪些功能之类的提示信息！！否则系统即将崩溃！！！";
                }
                TokenStream aiResult = aiCodeGeneratorService.generateVueProjectCodeStream(appId, userMessage);
                // 处理 TokenStream 转换为 Flux<String>
                yield processTokenStream(aiResult, appId);
            }
            default -> {
                String errorMessage = "不支持的生成类型：" + codeGenTypeEnum.getValue();
                throw new BusinessException(ResultEnum.SYSTEM_ERROR, errorMessage);
            }
        };
    }


    private static Flux<String> parserAndSaveResult(Flux<String> result, CodeGenTypeEnum codeGenTypeEnum, Long appId) {
        // 当流式返回生成代码完成后，再保存代码
        StringBuilder codeBuilder = new StringBuilder();
        // 实时收集代码片段
        return result
                .doOnNext(codeBuilder::append) // 实时收集代码片段
                .doOnComplete(() -> {
                    // 流式返回完成后保存代码
                    try {
                        String completeMultiFileCode = codeBuilder.toString();
                        // 使用解析器解析AI结果到 对象中
                        Object multiFileResult = CodeParserExecutor.executeParser(completeMultiFileCode, codeGenTypeEnum);
                        // 使用解析器 保存代码到文件
                        File savedDir = FileSaveExecutor.executeSave(multiFileResult, codeGenTypeEnum, appId);
                        log.info("保存成功，路径为：" + savedDir.getAbsolutePath());
                    } catch (Exception e) {
                        log.error("保存失败: {}", e.getMessage());
                    }
                });
    }

    /**
     * 将 TokenStream 转换为 Flux<String>，并传递工具调用信息
     *
     * @param tokenStream TokenStream 对象
     * @return Flux<String> 流式响应
     */
    private Flux<String> processTokenStream(TokenStream tokenStream, Long appId) {
        return Flux.create(sink -> {
            tokenStream.onPartialResponse((String partialResponse) -> {
                        AiResponseMessage aiResponseMessage = new AiResponseMessage(partialResponse);
                        sink.next(JSONUtil.toJsonStr(aiResponseMessage));
                    })
                    .onPartialToolExecutionRequest((index, toolExecutionRequest) -> {
                        ToolRequestMessage toolRequestMessage = new ToolRequestMessage(toolExecutionRequest);
                        sink.next(JSONUtil.toJsonStr(toolRequestMessage));
                    })
                    .onToolExecuted((ToolExecution toolExecution) -> {
                        ToolExecutedMessage toolExecutedMessage = new ToolExecutedMessage(toolExecution);
                        sink.next(JSONUtil.toJsonStr(toolExecutedMessage));
                    })
                    .onCompleteResponse((ChatResponse response) -> {
                        //TODO：可优化点一
                        /**
                         * 在所有消息处理完毕后，添加构建项目的提示信息，
                         * 并在下游的service层增加对提示信息的处理，可以把 提示信息也增加一个枚举，并封装成一个或对象
                         */
                        //sink.next("正在构建 Vue 项目，请稍候...");
                        // 同步构建 Vue 项目
                        String projectPath = AppConstant.CODE_OUTPUT_ROOT_DIR + "/vue_project_" + appId;
                        vueProjectBuilder.buildProject(projectPath);
                        sink.complete();
                    })
                    .onError((Throwable error) -> {
                        error.printStackTrace();
                        sink.error(error);
                    })
                    .start();
        });
    }


}
