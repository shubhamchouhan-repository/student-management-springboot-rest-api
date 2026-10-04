package com.example.sms.service;

import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

public interface FileStorageService {

    // store()  → save image
    String store(MultipartFile file);

    // load()   → read image
    Resource load(String fileName);

    // delete() → delete image
    void delete(String fileName);
}