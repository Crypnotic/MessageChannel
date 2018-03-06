/*
 * This file is part of MessageChannel, licensed under the MIT License (MIT).
 *
 * Copyright (c) Crypnotic <https://www.crypnotic.me>
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
package me.crypnotic.messagechannel.api.pipeline;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import lombok.SneakyThrows;
import me.crypnotic.messagechannel.api.exception.MessageChannelException;

public class PipelineMessage {

    private UUID target;
    private List<Object> content;
    private int index;

    public PipelineMessage() {
        this.content = new ArrayList<Object>();
        this.index = 0;
    }

    public PipelineMessage(UUID target) {
        this.target = target;
        this.content = new ArrayList<Object>();
        this.index = 0;
    }

    public PipelineMessage(UUID target, List<Object> content) {
        this.target = target;
        this.content = content;
        this.index = 0;
    }

    public void write(Object object) {
        content.add(object);
    }

    public Object read() throws MessageChannelException {
        if (index >= content.size()) {
            throw new MessageChannelException("List of size " + content.size() + " could not read object at " + index);
        }
        Object object = content.get(index);
        index += 1;
        return object;
    }

    @SneakyThrows(MessageChannelException.class)
    public <T> T read(Class<T> clazz) {
        Object object = read();
        if (object.getClass().isAssignableFrom(clazz)) {
            return clazz.cast(object);
        }
        throw new MessageChannelException("Could not cast " + object + " to class: " + clazz.getSimpleName());
    }

    public PipelineMessage clone() {
        return new PipelineMessage(target, content);
    }

    public UUID getTarget() {
        return target;
    }

    public List<Object> getContents() {
        return new ArrayList<Object>(content);
    }
}