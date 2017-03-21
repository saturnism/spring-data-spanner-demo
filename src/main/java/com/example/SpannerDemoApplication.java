/*
 * Copyright 2017 Google Inc. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example;

import com.google.cloud.spanner.Statement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.spanner.core.SpannerTemplate;

import java.util.List;
import java.util.UUID;

@SpringBootApplication
public class SpannerDemoApplication implements CommandLineRunner {
  @Autowired
  SpannerTemplate spannerTemplate;

  public static void main(String[] args) {
    SpringApplication.run(SpannerDemoApplication.class, args);
  }

  @Override
  public void run(String... strings) throws Exception {
     spannerTemplate.findAll(Trade.class)
        .stream().forEach(System.out::println);

    Trade t = Trade.builder().id(UUID.randomUUID().toString())
        .symbol("AAPL")
        .action("BUY")
        .shares(5.0)
        .price(100.0).build();

    spannerTemplate.insert(t);

    spannerTemplate.transaction(ctx -> {
      List<Trade> trades = ctx.find(Trade.class, Statement.of("select id, price from trades where symbol = 'AAPL'"));
      for (Trade trade: trades) {
        trade.setPrice(trade.getPrice() + 13);
        ctx.update(trade, "price");
      }
    });
  }
}
