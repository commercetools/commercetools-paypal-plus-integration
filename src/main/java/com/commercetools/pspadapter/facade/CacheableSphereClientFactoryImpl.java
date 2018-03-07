package com.commercetools.pspadapter.facade;

import com.commercetools.pspadapter.tenant.TenantConfig;
import com.commercetools.pspadapter.util.CtpClientConfigurationUtils;
import io.sphere.sdk.client.SphereClient;
import io.sphere.sdk.client.SphereClientConfig;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;

import javax.annotation.Nonnull;
import javax.annotation.Resource;

/**
 * {@link SphereClient} factory to allow caching sphere clients for {@link TenantConfig}s and {@link SphereClientConfig}.
 * <p>
 * This factory is a preferred way to create/get sphere clients for every tenant/sphereClient config
 * <p>
 * The main problem with non-cached clients is that they should be closed explicitly after an HTTP request, otherwise
 * they become lost non-garbaged values. But closing the clients every time is quite complicated solution for current
 * service implementation. Also, recreating sphere clients every time doesn't make much sense, so caching client for
 * constant configs is a good approach.
 * <p>
 * <b>Note:</b> current solution doesn't support any cache eviction. This is not a problem so far since
 * {@link SphereClient}s usually quite lightweight and we don't expect too much tenants on the same service. But if in
 * the future the service grows to some monstrous application - eviction should be used. See more here:<ul>
 * <li><a href="https://stackoverflow.com/questions/8181768/can-i-set-a-ttl-for-cacheable">Can I set a TTL for @Cacheable</a></li>
 * <li><a href="https://github.com/ben-manes/caffeine/wiki/Eviction">Caffein Eviction</a></li>
 * <li><b>Spring:</b><ul>
 * <li><a href="https://docs.spring.io/spring/docs/3.1.x/spring-framework-reference/htmlsingle/spring-framework-reference.html#cache-specific-config">
 * How can I set the TTL/TTI/Eviction policy/XXX feature?</a></li>
 * <li><a href="https://docs.spring.io/spring/docs/current/spring-framework-reference/integration.html#cache">Cache Abstraction</a></li>
 * <li><a href="https://docs.spring.io/spring/docs/current/javadoc-api/org/springframework/cache/annotation/CacheEvict.html">CacheEvict</a></li>
 * </ul></li>
 * </ul>
 */
@Component
@CacheConfig(cacheNames = "SphereClientFactoryCache")
public class CacheableSphereClientFactoryImpl implements SphereClientFactory {

    /**
     * Self-autowired reference to proxified bean of this class.
     * <p>
     * To allow re-using caching (inside {@link #createSphereClient(TenantConfig)} method) we have to call
     * self-autowired instances, otherwise (with direct method access over <b><code>this</code></b>)
     * Spring caching feature won't work.
     *
     * @see <a href="https://stackoverflow.com/questions/16899604/spring-cache-cacheable-not-working-while-calling-from-another-method-of-the-s">
     * Spring Cache @Cacheable - not working while calling from another method of the same bean</a>
     * @see <a href="https://stackoverflow.com/questions/5152686/self-injection-with-spring/5251930#5251930">
     * Self injection with Spring</a>
     */
    @Resource
    private SphereClientFactory self;

    @Override
    @Cacheable(sync = true)
    public SphereClient createSphereClient(@Nonnull TenantConfig tenantConfig) {
        return self.createSphereClient(tenantConfig.getSphereClientConfig());
    }

    @Override
    @Cacheable(sync = true)
    public SphereClient createSphereClient(@Nonnull SphereClientConfig clientConfig) {
        return CtpClientConfigurationUtils.createSphereClient(clientConfig);
    }
}
