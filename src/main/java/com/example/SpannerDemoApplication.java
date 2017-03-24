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

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.spanner.core.SpannerTemplate;
import org.springframework.data.spanner.repository.config.EnableSpannerRepositories;

import java.util.UUID;

@SpringBootApplication
@EnableSpannerRepositories
public class SpannerDemoApplication implements CommandLineRunner {
  @Autowired
  SpannerTemplate spannerTemplate;

  @Autowired
  TradeRepository tradeRepository;

  @Autowired
  TraderRepository traderRepository;

  public static void main(String[] args) {
    SpringApplication.run(SpannerDemoApplication.class, args);
  }

  @Override
  public void run(String... strings) throws Exception {
    final String traderId = UUID.randomUUID().toString();
    spannerTemplate.transaction(ctx -> {
      Trader trader = Trader.builder().id(traderId)
          .name("Ray")
          .build();

      ctx.insert(trader);
      for (int i = 0; i < 5; i++) {
        String tradeId = UUID.randomUUID().toString();

        Trade t = Trade.builder().id(tradeId)
            .symbol("GOOGL")
            .action("BUY")
            .shares(5.0)
            .traderId(traderId)
            .price(100.0).build();

        ctx.insert(t);
      }
    });

    spannerTemplate.findAll(Trade.class)
        .stream().forEach(System.out::println);

    long count = tradeRepository.count();
    System.out.println("There are " + count + " records");

    Trader trader = traderRepository.findOne(traderId);
    System.out.println(trader);


    /*
    tradeRepository.deleteAll();
    count = tradeRepository.count();
    System.out.println("Deleted, There are " + count + " records");
    */
  }
}
