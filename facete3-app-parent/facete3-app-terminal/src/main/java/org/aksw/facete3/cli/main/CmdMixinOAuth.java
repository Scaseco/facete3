package org.aksw.facete3.cli.main;

import java.util.Optional;

import picocli.CommandLine.ArgGroup;
import picocli.CommandLine.Option;

public class CmdMixinOAuth {

    @ArgGroup(exclusive = true)
    public OAuthOptions bearerToken = new OAuthOptions();

    public static class OAuthOptions {
        @Option(names = { "--bearer-token-value" }, description = "OAuth bearer token")
        public String value;

        @Option(names = { "--bearer-token-env-key" }, description = "Env variable from which to read the OAuth bearer token")
        public String envKey;

        /** Return the value or env-property for the given key. Blank strings count as null. */
        public String getEffectiveBearerToken() {
            String result = Optional.ofNullable(value)
                    .filter(x -> !x.isBlank())
                    .orElse(null);

            if (result == null) {
                result = Optional.ofNullable(envKey)
                        .map(System::getenv)
                        .filter(x -> !x.isBlank())
                        .orElse(null);
            }

            return result;
        }

    }


}
