package org.springframework.cloud.square.okhttp;

import java.util.Collections;
import java.util.List;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.cloud.client.loadbalancer.LoadBalancerClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.netflix.ribbon.Ribbon;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;

/**
 * @author Spencer Gibb
 */
@Configuration
@ConditionalOnClass({OkHttpClient.class, Ribbon.class})
@ConditionalOnBean(LoadBalancerClient.class)
@ConditionalOnProperty(value = "okhttp.ribbon.enabled", matchIfMissing = true)
public class OkHttpRibbonAutoConfiguration {

	static final String LOAD_BALANCED_CUSTOMIZED_BEAN_NAME = "loadBalancedOkHttpClientBuilderInitializer";

	@LoadBalanced
	@Autowired(required = false)
	private List<OkHttpClient.Builder> httpClientBuilders = Collections.emptyList();

	@Bean
	public OkHttpRibbonInterceptor okHttpRibbonInterceptor(LoadBalancerClient client) {
		return new OkHttpRibbonInterceptor(client);
	}

	@Bean(name = LOAD_BALANCED_CUSTOMIZED_BEAN_NAME)
	public InitializingBean loadBalancedOkHttpClientBuilderInitializer(
			final List<OkHttpClientBuilderCustomizer> customizers) {
		return () -> {
			for (OkHttpClient.Builder builder : OkHttpRibbonAutoConfiguration.this.httpClientBuilders) {
				for (OkHttpClientBuilderCustomizer customizer : customizers) {
					customizer.customize(builder);
				}
			}
		};
	}

	@Bean
	public OkHttpClientBuilderCustomizer okHttpClientBuilderCustomizer(List<Interceptor> interceptors) {
		return builder -> interceptors.forEach(builder::addInterceptor);
	}

}
