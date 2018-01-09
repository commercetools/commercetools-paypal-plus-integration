package com.commercetools.pspadapter.facade;

import com.commercetools.pspadapter.tenant.TenantConfig;
import com.commercetools.pspadapter.util.CtpClientConfigurationUtils;
import io.sphere.sdk.client.SphereClient;
import io.sphere.sdk.client.SphereClientConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.stereotype.Component;

import javax.annotation.Nonnull;

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
public class CacheableSphereClientFactoryImpl implements SphereClientFactory {

    private final SphereFactoryInternalCache sphereFactoryInternalCache;

    @Autowired
    public CacheableSphereClientFactoryImpl(@Nonnull SphereFactoryInternalCache sphereFactoryInternalCache) {
        this.sphereFactoryInternalCache = sphereFactoryInternalCache;
    }

    @Override
    public SphereClient createSphereClient(@Nonnull TenantConfig tenantConfig) {
        return sphereFactoryInternalCache.createSphereClient(tenantConfig.getSphereClientConfig());
    }

    @Override
    public SphereClient createSphereClient(@Nonnull SphereClientConfig clientConfig) {
        return sphereFactoryInternalCache.createSphereClient(clientConfig);
    }

    /**
     * Spring caching feature works over AOP proxies, thus internal calls to cached methods don't work. That's why
     * this internal bean is created: it "proxifies" overloaded {@code #createSphereClient(...)} methods
     * to real AOP proxified cacheable bean method {@link #createSphereClient}.
     *
     * @see <a href="https://stackoverflow.com/questions/16899604/spring-cache-cacheable-not-working-while-calling-from-another-method-of-the-s">Spring Cache @Cacheable - not working while calling from another method of the same bean</a>
     * @see <a href="https://stackoverflow.com/questions/12115996/spring-cache-cacheable-method-ignored-when-called-from-within-the-same-class">Spring cache @Cacheable method ignored when called from within the same class</a>
     */
    @EnableCaching
    @CacheConfig(cacheNames = "SphereClientFactoryCache")
    static class SphereFactoryInternalCache {

        @Cacheable(sync = true)
        public SphereClient createSphereClient(@Nonnull SphereClientConfig clientConfig) {
            return CtpClientConfigurationUtils.createSphereClient(clientConfig);
        }
    }
}
