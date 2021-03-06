/**
 * Copyright (c) 2016, Wang Wei (JCharm@aliyun.com) All rights reserved.  
 */
package io.github.jcharm.convert.bson;

import io.github.jcharm.convert.parser.AbstractSimpleParser;

/**
 * BSON格式双向序列化抽象解析器.
 *
 * @param <T> 序列化/反序列化数据类型
 */
public abstract class BsonSimpleParser<T> extends AbstractSimpleParser<BsonDeserializeReader, BsonSerializeWriter, T> {

}
