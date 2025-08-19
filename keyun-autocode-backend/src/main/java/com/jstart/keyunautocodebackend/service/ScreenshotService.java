package com.jstart.keyunautocodebackend.service;

public interface ScreenshotService {

    /**
     * 生成并上传网页截图
     *
     * @param webUrl 网页URL
     * @return 上传后的截图可访问URL
     */
    String generateAndUploadScreenshot(String webUrl);

}
