package org.maxpri.wagduck.util;

import org.maxpri.wagduck.domain.entity.AttributeDefinition;
import org.maxpri.wagduck.domain.entity.EntityDefinition;
import org.maxpri.wagduck.domain.entity.ProjectConfiguration;
import org.maxpri.wagduck.domain.enums.ProjectOptions;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component // Make it a Spring bean if used by MapStruct directly
public class NamingUtils {

    /**
     * Converts a name (like an entity name) to camelCase.
     * Example: "UserOrder" -> "userOrder"
     */
    public static String toCamelCase(String name) {
        if (name == null || name.isEmpty()) {
            return name;
        }
        if (name.length() == 1) {
            return name.toLowerCase();
        }
        return Character.toLowerCase(name.charAt(0)) + name.substring(1);
    }

     /**
     * Converts a name (like an entity name) to PascalCase (ClassName).
     * Example: "userOrder" -> "UserOrder"
     */
     public static String toPascalCase(String name) {
         if (name == null || name.isEmpty()) {
             return name;
         }
         if (name.length() == 1) {
             return name.toUpperCase();
         }
         return Character.toUpperCase(name.charAt(0)) + name.substring(1);
     }

    /**
     * Converts a name (like an attribute or class name) to snake_case.
     * Example: "UserOrder" -> "user_order"
     * Example: "userID" -> "user_id"
     * Example: "SimpleXMLParser" -> "simple_xml_parser"
     */
    public static String toSnakeCase(String name) {
        if (name == null || name.isEmpty()) {
            return name;
        }

        StringBuilder result = new StringBuilder();
        result.append(Character.toLowerCase(name.charAt(0))); // Start with the first character in lower case

        for (int i = 1; i < name.length(); i++) {
            char currentChar = name.charAt(i);
            // Check if it's an uppercase letter
            if (Character.isUpperCase(currentChar)) {
                // Check if the previous character was not uppercase (to handle acronyms like XML)
                // or if the next character is lowercase (to handle cases like UserID -> user_id)
                char previousChar = name.charAt(i - 1);
                boolean nextIsLowercase = (i < name.length() - 1) && Character.isLowerCase(name.charAt(i + 1));

                if (!Character.isUpperCase(previousChar) || nextIsLowercase) {
                    // Add underscore only if it's a transition from lower/digit to upper,
                    // or part of an acronym followed by a lowercase letter (e.g., XMLParser)
                    result.append('_');
                }
                result.append(Character.toLowerCase(currentChar));
            } else {
                result.append(currentChar); // Append other characters (lowercase, digits, etc.) directly
            }
        }

        return result.toString();
    }

    public static boolean isAuditAttribute(AttributeDefinition attr) {
        return attr.getAttributeName().equals("createdAt") || attr.getAttributeName().equals("updatedAt");
    }

    public static String findPrimaryKeyType(EntityDefinition entity) {
        return findPrimaryKeyAttribute(entity)
                .map(AttributeDefinition::getDataType)
                .map(dataType -> switch (dataType.toLowerCase()) {
                    case "integer" -> "Integer";
                    case "long" -> "Long";
                    case "uuid" -> "UUID";
                    case "string" -> "String";
                    default -> "Object";
                })
                .orElse("Long");
    }

    public static String findPrimaryKeyName(EntityDefinition entity) {
        return findPrimaryKeyAttribute(entity)
                .map(AttributeDefinition::getAttributeName)
                .orElse("id");
    }

    public static boolean checkProjectOption(ProjectConfiguration project, ProjectOptions option) {
        return project.getEnabledOptions() != null
                && project.getEnabledOptions().contains(option.name());
    }

    private static Optional<AttributeDefinition> findPrimaryKeyAttribute(EntityDefinition entity) {
        return entity.getAttributes().stream()
                .filter(AttributeDefinition::isPrimaryKey)
                .findFirst();
    }
}