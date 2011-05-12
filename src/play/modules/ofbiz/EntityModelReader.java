package play.modules.ofbiz;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;

import org.apache.commons.io.filefilter.SuffixFileFilter;
import org.ofbiz.entity.model.ModelViewEntity;

import play.Play;
import play.modules.ofbiz.generated.model.Alias;
import play.modules.ofbiz.generated.model.AliasAll;
import play.modules.ofbiz.generated.model.Entity;
import play.modules.ofbiz.generated.model.Entitymodel;
import play.modules.ofbiz.generated.model.Exclude;
import play.modules.ofbiz.generated.model.ExtendEntity;
import play.modules.ofbiz.generated.model.Field;
import play.modules.ofbiz.generated.model.MemberEntity;
import play.modules.ofbiz.generated.model.Relation;
import play.modules.ofbiz.generated.model.ViewEntity;
import play.templates.Template;
import play.templates.TemplateLoader;

public class EntityModelReader {
	
	public static void main(String[] args) throws Exception {
		if (args[0] == null) {
			System.out.println("Not directory for config files given, exiting...");
			System.exit(0);
		}
		new EntityModelReader(args[0]);
	}

	private Unmarshaller u;
	private Map<String, Entity> modelTreeEntity = new HashMap<String,Entity>();
	private Map<String, ViewEntity> modelTreeViewEntity = new HashMap<String,ViewEntity>();
	private Map<String, List<ExtendEntity>> modelTreeExtendEntity = new HashMap<String,List<ExtendEntity>>();
	
	public EntityModelReader(String directoryStr) throws Exception {
		Play.init(new File("."), "entityWriter");
		JAXBContext jc = JAXBContext.newInstance( "play.modules.ofbiz.generated.model" );
		u = jc.createUnmarshaller();

		File dir = new File(directoryStr);
		for (String fileName : dir.list(new SuffixFileFilter(".xml"))) {
			File file = new File(dir, fileName);
			System.out.println("Reading file " + file.getAbsolutePath());
			readEntityClassFileIntoTree(file);
		}
		System.out.println(String.format("Stats after reading: %s entities, %s view entities", modelTreeEntity.size(), modelTreeViewEntity.size()));

		for (Entity entity : modelTreeEntity.values()) {
			writeOutEntity(entity);
		}
		for (ViewEntity entity : modelTreeViewEntity.values()) {
			writeOutViewEntity(entity);
		}
	}
	
	private void readEntityClassFileIntoTree(File file) throws Exception {
		Entitymodel model = (Entitymodel) u.unmarshal(new FileInputStream(file) );
		
		List<Object> models = model.getEntityOrViewEntityOrExtendEntity();
		
		for (Object modelObj : models) {
			if (modelObj instanceof Entity) {
				Entity entity = (Entity) modelObj;
				modelTreeEntity.put(entity.getEntityName(), entity);
			} else if (modelObj instanceof ViewEntity) {
				ViewEntity entity = (ViewEntity) modelObj;
				modelTreeViewEntity.put(entity.getEntityName(), entity);
			} else if (modelObj instanceof ExtendEntity) {
				ExtendEntity entity = (ExtendEntity) modelObj;
				String name = entity.getEntityName();
				if (!modelTreeExtendEntity.containsKey(name)) {
					modelTreeExtendEntity.put(name, new ArrayList());
				}
				modelTreeExtendEntity.get(name).add(entity);
			} else {
				String msg = String.format("Not handling entity %s from file %s", modelObj.getClass().getName(), file.getAbsolutePath());
				System.out.println(msg);
			}
		}		
	}

	
	private void writeOutViewEntity(ViewEntity entity) throws Exception {
		TemplateEntity templateEntity = new TemplateEntity();
		templateEntity.packageName = "models";
		templateEntity.name = entity.getEntityName();
		
		// TODO: exlude and include stuff is not kept in mind yet
		
		// Loop through member entites and tree them
		Map<String, String> members = new HashMap<String,String>();
		for(MemberEntity memberEntity : entity.getMemberEntity()) {
			members.put(memberEntity.getEntityAlias(), memberEntity.getEntityName());
		}

		// TODO: Also use alias-all stuff
		List<String> fieldsAlreadyWritten = new ArrayList<String>();
		for (AliasAll aliasAll : entity.getAliasAll()) {
			String entityName = members.get(aliasAll.getEntityAlias());
			List<String> excludes = new ArrayList<String>();
			
			for (Exclude exclude : aliasAll.getExclude()) {
				excludes.add(exclude.getField());
			}
			
			List<String> fieldsToInclude = getFieldNamesFromEntity(entityName, excludes);
			// sb.append(String.format("    // From entity: %s\n    // Excluded fields: %s\n", entityName, excludes));
			for (String fieldToLookup : fieldsToInclude) {
				String fieldType = getFieldTypeFromEntity(entityName, fieldToLookup);
				if (fieldType != null) {
					boolean alreadyWritten = true;
					if (!fieldsAlreadyWritten.contains(fieldToLookup)) {
						alreadyWritten = false;
						fieldsAlreadyWritten.add(fieldToLookup);
					}
					templateEntity.aliases.add(new TemplateAlias(fieldToLookup, fieldType, entityName, alreadyWritten));
				} else {
					// TODO:
					templateEntity.aliases.add(new TemplateAlias(fieldToLookup, "FROM VIEW1", entityName, true));
				}

			}
		}
		
		// Loop through aliases and return them as fields
		for (Alias alias : entity.getAlias()) {
			// Ignore complex aliases for
			if (alias.getComplexAlias() != null) {
				System.out.println("Ignoring complex alias " + alias.getName() + " in entity " + entity.getEntityName());
				continue;
			}
			
			String entityAlias = alias.getEntityAlias();
			String entityToLookup = members.get(entityAlias);
			String fieldToLookup = alias.getField() != null ? alias.getField() : alias.getName();
			
			String fieldType = getFieldTypeFromEntity(entityToLookup, fieldToLookup);
			
			// TODO: also support fieldType if a view entity is referenced
			if (fieldType != null) {
				boolean alreadyWritten = true;
				if (!fieldsAlreadyWritten.contains(alias.getName())) {
					alreadyWritten = false;
					fieldsAlreadyWritten.add(alias.getName());
				}
				templateEntity.aliases.add(new TemplateAlias(alias.getName(), fieldType, entityToLookup, alreadyWritten));
			} else {
				// TODO
				templateEntity.aliases.add(new TemplateAlias(fieldToLookup, "FROM VIEW2", entityToLookup, true));
			}
			
		}
		
		addRelationsToTemplateEntity(entity.getRelation(), templateEntity);
		// Write out class
		render("ofbiz/OfbizViewEntity.txt", entity.getEntityName(), templateEntity);
	}
	
	private void addRelationsToTemplateEntity(List<Relation> relations, TemplateEntity templateEntity) {
		// Loop through relations and return them as classes
		for (Relation relation : relations) {
			String relEntity = relation.getRelEntityName();
			String relationName = relation.getTitle() != null ? relation.getTitle().replaceAll(" ", "") + relEntity: relEntity;
			templateEntity.relations.add(new TemplateRelation(relationName, relEntity, relation.getType()));
		}
	}
	
	private List<String> getFieldNamesFromEntity(String entityName, List<String> excludes) {
		List<String> fieldNames = new ArrayList<String>();
		Entity entity = modelTreeEntity.get(entityName);
		if (entity != null) {
			for(Field field : entity.getField()) {
				if (!excludes.contains(field.getName())) {
					fieldNames.add(field.getName());
				}
			}
		}
		return fieldNames;
	}

	private String getFieldTypeFromEntity(String entityName, String fieldName) {
		Entity entity = modelTreeEntity.get(entityName);
		if (entity != null) {
			// Special treatment for the four standard fields
			if ("lastUpdatedStamp".equals(fieldName) || "lastUpdatedTxStamp".equals(fieldName) ||
				"createdStamp".equals(fieldName) || "createdTxStamp".equals(fieldName)) {
				return "Timestamp";
			}
			for (Field field : entity.getField()) {
				if (field.getName().equals(fieldName)) {
					return getClassForFieldType(field.getType());
				}
			}
		} else {
//			// Check deeper for 
//			ViewEntity viewEntity = modelTreeViewEntity.get(entity);
//			viewEntity.
		}
		return null;
	}

	private void writeOutEntity(Entity entity) throws IOException {
		TemplateEntity templateEntity = new TemplateEntity();
		templateEntity.packageName = "models";
		templateEntity.name = entity.getEntityName();
		
		for (Field field : entity.getField()) {
			String fieldDefinition = getClassForFieldType(field.getType());
			templateEntity.fields.add(new TemplateField(field.getName(), fieldDefinition));
		}
		
		// Add the four default timestamps
		templateEntity.fields.add(new TemplateField("lastUpdatedStamp", "Timestamp"));
		templateEntity.fields.add(new TemplateField("lastUpdatedTxStamp", "Timestamp"));
		templateEntity.fields.add(new TemplateField("createdStamp", "Timestamp"));
		templateEntity.fields.add(new TemplateField("createdTxStamp", "Timestamp"));
		
		// Check whether this entity has been extended
		if (modelTreeExtendEntity.containsKey(entity.getEntityName())) {
			List<ExtendEntity> extendEntities = modelTreeExtendEntity.get(entity.getEntityName());
			for (ExtendEntity extendEntity : extendEntities) {
				for (Field field : extendEntity.getField()) {
					String fieldDefinition = getClassForFieldType(field.getType());
					templateEntity.fields.add(new TemplateField(field.getName(), fieldDefinition, "extended"));
				}
			}
		}
		
		addRelationsToTemplateEntity(entity.getRelation(), templateEntity);
		
		render("ofbiz/OfbizEntity.txt", entity.getEntityName(), templateEntity);
	}
	
	public void render(String templateName, String entityName, TemplateEntity templateEntity) throws IOException {
		Template template = TemplateLoader.load(templateName);
		FileWriter fw = new FileWriter("app/models/" + entityName + ".java");
		Map<String, Object> context = new HashMap<String, Object>();
		context.put("entity", templateEntity);
		fw.append(template.render(context));
		fw.close();
	}
	
	private String getClassForFieldType(String type) {
		if ("blob".equals(type)) {
			return "java.sql.Blob";
		} else if ("object".equals(type)) {
			return "Object";
		} else if ("byte-array".equals(type)) {
			return "byte[]";
		// BigDecimal
		} else if ("currency-precise".equals(type)) {
			return "BigDecimal";
		} else if ("currency-amount".equals(type)) {
			return "BigDecimal";
		} else if ("fixed-point".equals(type)) {
			return "BigDecimal";
		// DATE relevant
		} else if ("date".equals(type)) {
			return "Date";
		} else if ("time".equals(type)) {
			return "Time";
		} else if ("date-time".equals(type)) {
			return "Timestamp";
		// Numbers
		} else if ("numeric".equals(type)) {
			return "Long";
		} else if ("floating-point".equals(type)) {
			return "Double";
		}
		// all others are string
		return "String";
	}
	
	public static class TemplateEntity {
		public String name;
		public String packageName;
//		public Set<String> imports = new LinkedHashSet<String>();
		
		public Set<TemplateField> fields = new LinkedHashSet();
		public Set<TemplateRelation> relations= new LinkedHashSet();
		public Set<TemplateAlias> aliases= new LinkedHashSet();
	}
	
	public static class TemplateField {
		public String name;
		public String type;
		public String comment;
		
		public TemplateField(String name, String type) {
			this.name = name;
			this.type = type;
		}
		public TemplateField(String name, String type, String comment) {
			this(name, type);
			this.comment = comment;
		}
	}
	
	public static class TemplateRelation extends TemplateField {
		public String assoc;
		
		public TemplateRelation(String name, String type, String assoc) {
			super(name, type);
			this.assoc = assoc;
		}
	}
	
	public static class TemplateAlias extends TemplateField {
		public String sourceEntity;
		public Boolean commentedOut;
		
		public TemplateAlias(String name, String type, String sourceEntity, Boolean commentedOut) {
			super(name, type);
			this.sourceEntity = sourceEntity;
			this.commentedOut = commentedOut;
		}
	}
}
