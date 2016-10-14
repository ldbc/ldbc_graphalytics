package nl.tudelft.graphalytics.util.json;

import com.google.gson.ExclusionStrategy;
import com.google.gson.FieldAttributes;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Created by wing on 27-11-14.
 */
public class JsonUtil {

    public static String toJson(Object object) {
        // http://stackoverflow.com/questions/4802887/gson-how-to-exclude-specific-fields-from-serialization-without-annotations
        Gson gson = new GsonBuilder().setExclusionStrategies(new AnnotationExclusionStrategy()).create();
        return gson.toJson(object);
    }

    public static String toPrettyJson(Object object) {
        // http://stackoverflow.com/questions/4802887/gson-how-to-exclude-specific-fields-from-serialization-without-annotations
        Gson gson = new GsonBuilder().setExclusionStrategies(new AnnotationExclusionStrategy()).setPrettyPrinting().create();
        return gson.toJson(object);
    }


    public static Object fromJson(String jsonString, Class clazz) {
        return (new Gson()).fromJson(jsonString, clazz);
    }

    public static Object createNewInstance(Object object) {
        return JsonUtil.fromJson(JsonUtil.toJson(object), object.getClass());
    }



    public static class AnnotationExclusionStrategy implements ExclusionStrategy {

        @Override
        public boolean shouldSkipField(FieldAttributes f) {
            return f.getAnnotation(Exclude.class) != null;
        }

        @Override
        public boolean shouldSkipClass(Class<?> clazz) {
            return false;
        }


    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.FIELD)
    public @interface Exclude {
    }

}
