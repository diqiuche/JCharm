/**
 * Copyright (c) 2016, Wang Wei (JCharm@aliyun.com) All rights reserved.  
 */
package io.github.jcharm.convert.json;

import io.github.jcharm.convert.parser.AbstractSimpleParser;

/**
 * JSON格式双向序列化抽象解析器.
 *
 * @param <T> 序列化/反序列化数据类型
 */
public abstract class JsonSimpleParser<T> extends AbstractSimpleParser<JsonDeserializeReader, JsonSerializeWriter, T> {

}
