/*
 * Copyright 1999-2015 dangdang.com.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * </p>
 */

package io.elasticjob.cloud.scheduler;

import io.elasticjob.cloud.scheduler.ha.HANode;
import io.elasticjob.cloud.scheduler.env.BootstrapEnvironment;
import io.elasticjob.cloud.scheduler.ha.SchedulerElectionCandidate;
import io.elasticjob.cloud.reg.base.CoordinatorRegistryCenter;
import io.elasticjob.cloud.reg.zookeeper.ZookeeperElectionService;
import io.elasticjob.cloud.reg.zookeeper.ZookeeperRegistryCenter;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.curator.framework.CuratorFramework;

import java.util.concurrent.CountDownLatch;

/**
 * Mesos框架启动器.
 *
 * @author caohao
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@Slf4j
public final class Bootstrap {
    
    /**
     * 启动入口.
     * 
     * @param args 命令行参数无需传入
     * @throws InterruptedException 线程中断异常
     */
    // CHECKSTYLE:OFF
    public static void main(final String[] args) throws InterruptedException {
        // CHECKSTYLE:ON
        CoordinatorRegistryCenter regCenter = new ZookeeperRegistryCenter(BootstrapEnvironment.getInstance().getZookeeperConfiguration());
        regCenter.init();
        final ZookeeperElectionService electionService = new ZookeeperElectionService(
                BootstrapEnvironment.getInstance().getFrameworkHostPort(), (CuratorFramework) regCenter.getRawClient(), HANode.ELECTION_NODE, new SchedulerElectionCandidate(regCenter));
        electionService.start();
        final CountDownLatch latch = new CountDownLatch(1);
        latch.await();
        Runtime.getRuntime().addShutdownHook(new Thread("shutdown-hook") {
        
            @Override
            public void run() {
                electionService.stop();
                latch.countDown();
            }
        });
    }
}
