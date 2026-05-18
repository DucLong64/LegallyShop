package com.legallyshop.legallyshop.product.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.Uploader;
import com.legallyshop.legallyshop.common.exception.AppException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CloudinaryServiceTest {

    @Mock
    private Cloudinary cloudinary;

    @Mock
    private Uploader uploader;

    @InjectMocks
    private CloudinaryService cloudinaryService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(cloudinaryService, "folder", "yourshop");
        when(cloudinary.uploader()).thenReturn(uploader);
    }

    @Test
    void upload_Success() throws Exception {
        MultipartFile file = new MockMultipartFile(
                "file",
                "test.jpg",
                "image/jpeg",
                "test image content".getBytes()
        );

        Map<String, Object> mockResult = Map.of("secure_url", "https://res.cloudinary.com/demo/image/upload/v123/yourshop/products/abc.jpg");
        when(uploader.upload(any(byte[].class), any(Map.class))).thenReturn(mockResult);

        String url = cloudinaryService.upload(file, "products");

        assertNotNull(url);
        assertEquals("https://res.cloudinary.com/demo/image/upload/v123/yourshop/products/abc.jpg", url);
        verify(uploader).upload(any(byte[].class), any(Map.class));
    }

    @Test
    void upload_NullFile_ThrowsException() {
        AppException exception = assertThrows(AppException.class, () -> {
            cloudinaryService.upload(null, "products");
        });
        assertTrue(exception.getMessage().contains("File ảnh không được trống"));
    }

    @Test
    void upload_EmptyFile_ThrowsException() {
        MultipartFile file = new MockMultipartFile("file", "test.jpg", "image/jpeg", new byte[0]);

        AppException exception = assertThrows(AppException.class, () -> {
            cloudinaryService.upload(file, "products");
        });
        assertTrue(exception.getMessage().contains("File ảnh không được trống"));
    }

    @Test
    void upload_InvalidContentType_ThrowsException() {
        MultipartFile file = new MockMultipartFile("file", "test.txt", "text/plain", "content".getBytes());

        AppException exception = assertThrows(AppException.class, () -> {
            cloudinaryService.upload(file, "products");
        });
        assertTrue(exception.getMessage().contains("File phải là ảnh"));
    }

    @Test
    void upload_FileTooLarge_ThrowsException() {
        byte[] largeContent = new byte[6 * 1024 * 1024]; // 6MB
        MultipartFile file = new MockMultipartFile("file", "test.jpg", "image/jpeg", largeContent);

        AppException exception = assertThrows(AppException.class, () -> {
            cloudinaryService.upload(file, "products");
        });
        assertTrue(exception.getMessage().contains("không được vượt quá 5MB"));
    }

    @Test
    void uploadThumbnail_Success() throws Exception {
        MultipartFile file = new MockMultipartFile("file", "test.jpg", "image/jpeg", "test".getBytes());
        Map<String, Object> mockResult = Map.of("secure_url", "https://res.cloudinary.com/demo/thumb.jpg");
        when(uploader.upload(any(byte[].class), any(Map.class))).thenReturn(mockResult);

        String url = cloudinaryService.uploadThumbnail(file, "products");

        assertNotNull(url);
        assertEquals("https://res.cloudinary.com/demo/thumb.jpg", url);
    }

    @Test
    void delete_Success() throws Exception {
        String imageUrl = "https://res.cloudinary.com/demo/image/upload/v123/yourshop/products/abc.jpg";
        when(uploader.destroy(eq("yourshop/products/abc"), any(Map.class))).thenReturn(Map.of("result", "ok"));

        cloudinaryService.delete(imageUrl);

        verify(uploader).destroy(eq("yourshop/products/abc"), any(Map.class));
    }

    @Test
    void delete_NullUrl_DoesNothing() throws Exception {
        cloudinaryService.delete(null);
        verify(uploader, never()).destroy(any(), any());
    }

    @Test
    void delete_BlankUrl_DoesNothing() throws Exception {
        cloudinaryService.delete("   ");
        verify(uploader, never()).destroy(any(), any());
    }
}
