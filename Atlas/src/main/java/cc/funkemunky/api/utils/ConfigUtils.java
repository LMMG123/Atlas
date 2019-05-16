package cc.funkemunky.api.utils;

import com.google.common.base.Charsets;
import com.google.common.io.Files;
import lombok.val;
import org.apache.commons.lang.Validate;
import org.bukkit.configuration.Configuration;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.configuration.file.YamlConfigurationOptions;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.emitter.Emitter;
import org.yaml.snakeyaml.error.YAMLException;
import org.yaml.snakeyaml.nodes.Node;
import org.yaml.snakeyaml.nodes.Tag;
import org.yaml.snakeyaml.representer.Representer;
import org.yaml.snakeyaml.serializer.Serializer;

import java.io.*;
import java.nio.charset.Charset;
import java.util.*;

public class ConfigUtils {

    public static void addCommentToLine(File file, String line, String comment) {

    }

    private static void save(File file) throws IOException {
        Validate.notNull(file, "File cannot be null");
        Files.createParentDirs(file);
        String data = this.saveToString();
        OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(file), UTF8_OVERRIDE && !UTF_BIG ? Charsets.UTF_8 : Charset.defaultCharset());

        try {
            writer.write(data);
        } finally {
            writer.close();
        }

    }

    private static String saveToString(YamlConfiguration yaml) {
        val field = ReflectionsUtil.getFieldByName(YamlConfiguration.class, "yamlOptions");
        val yamlOptions = (DumperOptions) ReflectionsUtil.getFieldValue(field, yaml);
        val rField = ReflectionsUtil.getFieldByName(YamlConfiguration.class, "yamlRepresenter");
        val yamlRepresenter = (Representer) ReflectionsUtil.getFieldValue(rField, yaml);
        yamlOptions.setIndent(options(yaml).indent());
        yamlOptions.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
        yamlOptions.setAllowUnicode("UTF-8");
        yamlRepresenter.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
        String header = buildHeader(yaml);
        String dump = dump(this.getValues(false));
        if (dump.equals("{}\n")) {
            dump = "";
        }

        return header + dump;
    }

    private static String dump(YamlConfiguration config, Object data) {
        List<Object> list = new ArrayList(1);
        list.add(data);
        return dumpAll(list.iterator());
    }

    public static String dumpAll(Iterator<? extends Object> data) {
        StringWriter buffer = new StringWriter();
        dumpAll(data, buffer, null);
        return buffer.toString();
    }

    public void dumpAll(Iterator<? extends Object> data, Writer output) {
        this.dumpAll(data, output, (Tag)null);
    }

    private static void dumpAll(Iterator<? extends Object> data, Writer output, Tag rootTag) {
        Serializer serializer = new Serializer(new Emitter(output, this.dumperOptions), this.resolver, this.dumperOptions, rootTag);

        try {
            serializer.open();

            while(data.hasNext()) {
                Node node = this.representer.represent(data.next());
                serializer.serialize(node);
            }

            serializer.close();
        } catch (IOException var6) {
            throw new YAMLException(var6);
        }
    }

    private static static YamlConfigurationOptions options(FileConfiguration config) {
        val method = ReflectionsUtil.getMethod(YamlConfiguration.class, "options");
        return (YamlConfigurationOptions) ReflectionsUtil.getMethodValue(method, config);
    }

    protected String buildHeader(YamlConfiguration config) {
        String header = this.options(config).header();
        if (this.options(config).copyHeader()) {
            Configuration def = config.getDefaults();
            if (def != null && def instanceof FileConfiguration) {
                FileConfiguration filedefaults = (FileConfiguration)def;
                String defaultsHeader = buildHeader(config);
                if (defaultsHeader != null && defaultsHeader.length() > 0) {
                    return defaultsHeader;
                }
            }
        }

        if (header == null) {
            return "";
        } else {
            StringBuilder builder = new StringBuilder();
            String[] lines = header.split("\r?\n", -1);
            boolean startedHeader = false;

            for(int i = lines.length - 1; i >= 0; --i) {
                builder.insert(0, "\n");
                if (startedHeader || lines[i].length() != 0) {
                    builder.insert(0, lines[i]);
                    builder.insert(0, "# ");
                    startedHeader = true;
                }
            }

            return builder.toString();
        }
    }
}
