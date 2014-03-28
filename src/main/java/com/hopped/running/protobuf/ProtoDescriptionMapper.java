/*!
 * Copyright (c) 2014 Dennis Hoppe
 * www.dennis-hoppe.com
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package com.hopped.running.protobuf;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import com.google.protobuf.DescriptorProtos.FileDescriptorProto;
import com.google.protobuf.DescriptorProtos.FileDescriptorSet;
import com.google.protobuf.Descriptors.Descriptor;
import com.google.protobuf.Descriptors.DescriptorValidationException;
import com.google.protobuf.Descriptors.FileDescriptor;
import com.google.protobuf.Message;

/**
 * @author Dennis Hoppe (hoppe.dennis@ymail.com)
 * 
 */
public enum ProtoDescriptionMapper {
    INSTANCE;

    private Map<String, String> mapping = new HashMap<String, String>();

    /**
     * 
     * @param filename
     */
    public void generateMapping(final String filename) {
        try {
            FileInputStream fis = new FileInputStream(filename);
            FileDescriptorSet descriptorSet = FileDescriptorSet.parseFrom(fis);
            for (FileDescriptorProto fdp : descriptorSet.getFileList()) {
                FileDescriptor fd = FileDescriptor.buildFrom(fdp,
                        new FileDescriptor[] {});

                for (Descriptor descriptor : fd.getMessageTypes()) {
                    String className = fdp.getOptions().getJavaPackage() + "."
                            + fdp.getOptions().getJavaOuterClassname() + "$"
                            + descriptor.getName();
                    mapping.put(descriptor.getFullName(), className);
                }
            }
        } catch (FileNotFoundException fnfe) {
            throw new IllegalArgumentException(fnfe.getMessage());
        } catch (DescriptorValidationException | IOException e) {
            throw new IllegalStateException(e);
        }
    }

    /**
     * 
     * @param descriptor
     */
    public final String getClassBy(final String descriptor) {
        return mapping.get(descriptor);
    }

    /**
     * 
     * @param message
     * @return
     * @throws IOException
     */
    public byte[] messageToByteBuffer(Message message)
            throws IOException {
        byte[] name = message.getDescriptorForType().getFullName()
                .getBytes("UTF-8");
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        baos.write(name.length);
        baos.write(name);
        byte[] messageBytes = message.toByteArray();
        baos.write(messageBytes.length);
        baos.write(messageBytes);
        baos.flush();

        return baos.toByteArray();
    }

    /**
     * https://planet.jboss.org/post/
     * generic_marshalling_with_google_protocol_buffers
     * ;jsessionid=8B818CBA5012141CE0AC62E9BADB130A
     * 
     * @param buffer
     * @return
     * @throws Exception
     */
    public Object objectFromByteBuffer(byte[] buffer) throws Exception {
        ByteArrayInputStream bais = new ByteArrayInputStream(buffer);
        byte[] name = new byte[bais.read()];
        bais.read(name);
        String className = mapping.get(new String(name, "UTF-8"));
        Class<?> clazz = Thread.currentThread()
                .getContextClassLoader()
                .loadClass(className);
        Method parseFromMethod = clazz.getMethod("parseFrom", byte[].class);
        byte[] message = new byte[bais.read()];
        bais.read(message);

        return parseFromMethod.invoke(null, message);
    }

}
