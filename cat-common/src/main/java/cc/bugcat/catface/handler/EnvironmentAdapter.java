package cc.bugcat.catface.handler;

import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.context.expression.BeanFactoryAccessor;
import org.springframework.context.expression.MapAccessor;
import org.springframework.core.env.PropertiesPropertySource;
import org.springframework.core.env.PropertyResolver;
import org.springframework.core.env.StandardEnvironment;
import org.springframework.expression.AccessException;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.Expression;
import org.springframework.expression.ParserContext;
import org.springframework.expression.PropertyAccessor;
import org.springframework.expression.TypedValue;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;

import java.util.Collections;
import java.util.Map;
import java.util.Properties;
import java.util.function.Function;

public abstract class EnvironmentAdapter {


    /**
     * 适配Spring环境变量
     * */
    public static EnvironmentAdapter environmentProperty(ConfigurableListableBeanFactory configurableBeanFactory){
        return new ContainerEnvironment(configurableBeanFactory);
    }
    
    /**
     * 适配自定义环境变量
     * */
    public static EnvironmentAdapter environmentProperty(Properties property){
        return new StandardsEnvironment(property);
    }


    /**
     * 基于adapter创建新的容器
     * */
    public static EnvironmentAdapter newAdapter(EnvironmentAdapter adapter, Map<?, Object> container){
        return adapter.newAdapter(container);
    }


    /**
     * 从容器中读取参数。
     * @param express ${user.id} or springEL
     * */
    public String getProperty(String express) {
        return getProperty(express, String.class);
    }
    
    /**
     * 从容器中读取参数。
     * @param express ${user.id} or springEL
     * */
    public <T> T getProperty(String express, Class<T> clazz) {
        return (T) getProperty(express, (T) null);
    }

    /**
     * 从容器中读取参数。
     * @param express ${user.id} or springEL
     * @param defaultValue 默认值
     * */
    public abstract <T> T getProperty(String express, T defaultValue);


    
    /**
     * 创建新的容器
     * */
    protected abstract EnvironmentAdapter newAdapter(Map<?, Object> container);
    
    
    
    
    /**
     * Spring容器
     * */
    private static class ContainerEnvironment extends EnvironmentAdapter {
        private final SpelExpressionParser expressionResolver;
        private final EvaluationContext evaluationContext;
        private final ConfigurableListableBeanFactory configurableBeanFactory;
        
        private ContainerEnvironment(ConfigurableListableBeanFactory configurableBeanFactory) {
            this.configurableBeanFactory = configurableBeanFactory;
            this.expressionResolver = new SpelExpressionParser();
            this.evaluationContext = getEvaluationContext(Collections.EMPTY_MAP);
        }

        /**
         * 从容器中读取参数。
         * @param express ${user.id} or springEL
         * @param defaultValue 默认值
         * */
        @Override
        public <T> T getProperty(String express, T defaultValue) {
            String expressValue = configurableBeanFactory.resolveEmbeddedValue(express);
            Object value = evaluateValue(expressValue, evaluationContext);
            return value == null ? defaultValue : (T) value;
        }

        @Override
        protected EnvironmentAdapter newAdapter(Map<?, Object> container) {
            EvaluationContext context = getEvaluationContext(container);
            Function<String, Object> randerExpress = express -> {
                String expressValue = configurableBeanFactory.resolveEmbeddedValue(express);
                Object value = evaluateValue(expressValue, context);
                return value;
            };

            SimpleEnvironment adapter = new SimpleEnvironment(randerExpress);
            return adapter;
        }

        /**
         * 执行springEL
         * */
        private Object evaluateValue(String expressValue, EvaluationContext context) {
            Expression expression = expressionResolver.parseExpression(expressValue, ParserContext.TEMPLATE_EXPRESSION);
            return expression.getValue(context);
        }
        
        private EvaluationContext getEvaluationContext(Map<?, Object> beanMap){
            StandardEvaluationContext evaluationContext = new StandardEvaluationContext(configurableBeanFactory);
            evaluationContext.getPropertyAccessors().add(new PropertyAccessorDecorate(new BeanFactoryAccessor(), beanMap));
            return evaluationContext;
        }
    }


    /**
     * 静态调用容器
     * */
    private static class StandardsEnvironment extends EnvironmentAdapter {
        private final SpelExpressionParser expressionResolver;
        private final EvaluationContext evaluationContext;
        private final Properties property;
        private final PropertyResolver resolver;
        
        private StandardsEnvironment(Properties property) {
            PropertiesPropertySource propertySource = new PropertiesPropertySource("catToolsProperty", property);
            StandardEnvironment source = new StandardEnvironment();
            source.getPropertySources().addLast(propertySource);
            
            this.property = property;
            this.resolver = source;
            this.expressionResolver = new SpelExpressionParser();
            this.evaluationContext = getEvaluationContext(Collections.EMPTY_MAP);
        }

        /**
         * 从容器中读取参数。
         * @param express ${user.id} or springEL
         * @param defaultValue 默认值
         * */
        @Override
        public <T> T getProperty(String express, T defaultValue) {
            String expressValue = resolver.resolvePlaceholders(express);
            Object value = evaluateValue(expressValue, evaluationContext);
            return value == null ? defaultValue : (T) value;
        }

        @Override
        protected EnvironmentAdapter newAdapter(Map<?, Object> container) {
            EvaluationContext context = getEvaluationContext(container);
            Function<String, Object> randerExpress = express -> {
                String expressValue = resolver.resolvePlaceholders(express);
                Object value = evaluateValue(expressValue, context);
                return value;
            };
            SimpleEnvironment adapter = new SimpleEnvironment(randerExpress);
            return adapter;
        }
        
        
        /**
         * 执行springEL
         * */
        private Object evaluateValue(String expressValue, EvaluationContext context) {
            Expression expression = expressionResolver.parseExpression(expressValue, ParserContext.TEMPLATE_EXPRESSION);
            return expression.getValue(context);
        }


        private EvaluationContext getEvaluationContext(Map<?, Object> beanMap){
            StandardEvaluationContext evaluationContext = new StandardEvaluationContext(property);
            evaluationContext.addPropertyAccessor(new PropertyAccessorDecorate(new MapAccessor(), beanMap));
            return evaluationContext;
        }
    }
    
    
    /**
     * 使用新的容器解析
     * */
    private static class SimpleEnvironment extends EnvironmentAdapter {
        private final Function<String, Object> randerExpress;
        private SimpleEnvironment(Function<String, Object> randerExpress) {
            this.randerExpress = randerExpress;
        }
        
        @Override
        public <T> T getProperty(String express, T defaultValue) {
            Object value = randerExpress.apply(express);
            return value == null ? defaultValue : (T) value;
        }

        @Override
        protected EnvironmentAdapter newAdapter(Map<?, Object> container) {
            throw new UnsupportedOperationException();
        }
    }
    
    
    
    private static class PropertyAccessorDecorate implements PropertyAccessor {
        private final PropertyAccessor bridge;
        private final Map<?, Object> contextMap;
        private PropertyAccessorDecorate(PropertyAccessor bridge, Map<?, Object> contextMap) {
            this.bridge = bridge;
            this.contextMap = contextMap;
        }

        @Override
        public boolean canRead(EvaluationContext context, Object target, String name) throws AccessException {
            if ( contextMap.containsKey(name) ) {
                return true;
            }
            return bridge.canRead(context, target, name);
        }

        @Override
        public TypedValue read(EvaluationContext context, Object target, String name) throws AccessException {
            Object bean = contextMap.get(name);
            if( bean != null ){
                return new TypedValue(bean);
            }
            return bridge.read(context, target, name);
        }

        @Override
        public Class<?>[] getSpecificTargetClasses() {
            return bridge.getSpecificTargetClasses();
        }
        
        @Override
        public boolean canWrite(EvaluationContext context, Object target, String name) throws AccessException {
            return bridge.canWrite(context, target, name);
        }

        @Override
        public void write(EvaluationContext context, Object target, String name, Object newValue) throws AccessException {
            bridge.write(context, target, name, newValue);
        }
    }
}
