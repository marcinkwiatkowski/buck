/*
 * Copyright 2014-present Facebook, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may
 * not use this file except in compliance with the License. You may obtain
 * a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */

package com.facebook.buck.apple;

import com.facebook.buck.cli.BuckConfig;
import com.facebook.buck.cli.FakeBuckConfig;
import com.facebook.buck.testutil.FakeProjectFilesystem;
import com.facebook.buck.testutil.TestConsole;
import com.facebook.buck.util.DefaultProcessExecutor;
import com.facebook.buck.util.ProcessExecutor;
import com.google.common.collect.ImmutableMap;
import java.util.Optional;

public class AppleNativeIntegrationTestUtils {

  private AppleNativeIntegrationTestUtils() {}

  private static ImmutableMap<AppleSdk, AppleSdkPaths> discoverSystemSdkPaths(
      BuckConfig buckConfig) {
    AppleConfig appleConfig = buckConfig.getView(AppleConfig.class);
    ProcessExecutor executor = new DefaultProcessExecutor(new TestConsole());
    return appleConfig.getAppleSdkPaths(executor);
  }

  private static Optional<AppleSdk> anySdkForPlatform(
      final ApplePlatform platform, final ImmutableMap<AppleSdk, AppleSdkPaths> sdkPaths) {
    return sdkPaths
        .keySet()
        .stream()
        .filter(sdk -> sdk.getApplePlatform().equals(platform))
        .findFirst();
  }

  public static boolean isApplePlatformAvailable(ApplePlatform platform) {
    BuckConfig buckConfig = FakeBuckConfig.builder().build();
    return anySdkForPlatform(platform, discoverSystemSdkPaths(buckConfig)).isPresent();
  }

  public static boolean isSwiftAvailable(ApplePlatform platform) {
    BuckConfig buckConfig = FakeBuckConfig.builder().build();
    ImmutableMap<AppleSdk, AppleSdkPaths> sdkPaths = discoverSystemSdkPaths(buckConfig);
    Optional<AppleSdk> anySdkOptional = anySdkForPlatform(platform, sdkPaths);
    if (!anySdkOptional.isPresent()) {
      return false;
    }
    AppleSdk anySdk = anySdkOptional.get();
    AppleCxxPlatform appleCxxPlatform =
        AppleCxxPlatforms.buildWithExecutableChecker(
            new FakeProjectFilesystem(),
            anySdk,
            "fakeversion",
            "fakearch",
            sdkPaths.get(anySdk),
            buckConfig,
            new XcodeToolFinder(),
            FakeAppleRuleDescriptions.FAKE_XCODE_BUILD_VERSION_CACHE,
            Optional.empty());
    return appleCxxPlatform.getSwiftPlatform().isPresent();
  }
}
