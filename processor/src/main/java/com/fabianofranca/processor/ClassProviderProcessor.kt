package com.fabianofranca.processor

import com.fabianofranca.annotations.GenerateClassProvider
import com.google.auto.service.AutoService
import com.squareup.kotlinpoet.*
import java.io.File
import javax.annotation.processing.*
import javax.lang.model.SourceVersion
import javax.lang.model.element.TypeElement

@AutoService(Processor::class)
@SupportedSourceVersion(SourceVersion.RELEASE_8)
@SupportedOptions(GenerateClassProviderProcessor.KAPT_KOTLIN_GENERATED_OPTION_NAME)
class GenerateClassProviderProcessor : AbstractProcessor() {

    override fun process(
        annotations: MutableSet<out TypeElement>,
        roundEnvironment: RoundEnvironment
    ): Boolean {

        roundEnvironment.getElementsAnnotatedWith(GenerateClassProvider::class.java)
            .forEach {
                val path =
                    processingEnv.options[KAPT_KOTLIN_GENERATED_OPTION_NAME].orEmpty()

                if (path.isEmpty()) {
                    error(TARGET_DIRECTORY_NOT_FOUND)
                    return false
                }

                createClass(
                    path,
                    processingEnv.elementUtils.getPackageOf(it).toString(),
                    it.simpleName.toString()
                )
            }

        return false
    }

    private fun createClass(path: String, packageName: String, simpleName: String) {

        val file = File(path)

        file.mkdir()

        val className = "$simpleName$CLASS_SUFFIX"

        val classProvider = ClassName(CLASS_PROVIDER_PACKAGE, CLASS_PROVIDER_NAME)
        val classInstanceType = TypeCollection::classStarProjection.returnType.asTypeName()

        FileSpec.builder(packageName, className)
            .addType(
                TypeSpec.classBuilder(className)
                    .addSuperinterface(classProvider)
                    .primaryConstructor(
                        FunSpec.constructorBuilder()
                            .addParameter(
                                ParameterSpec.builder(
                                    CLASS_PROVIDER_PROPERTY_NAME,
                                    classInstanceType
                                )
                                    .defaultValue("$simpleName::class.java")
                                    .build()
                            )
                            .build()
                    )
                    .addProperty(
                        PropertySpec.builder(
                            CLASS_PROVIDER_PROPERTY_NAME,
                            classInstanceType,
                            KModifier.OVERRIDE
                        )
                            .initializer(CLASS_PROVIDER_PROPERTY_NAME)
                            .build()
                    )
                    .build()
            )
            .build()
            .writeTo(file)

    }

    override fun getSupportedAnnotationTypes(): MutableSet<String> {
        return mutableSetOf(GenerateClassProvider::class.java.canonicalName)
    }

    private fun error(message: String) {
        processingEnv.messager.printMessage(javax.tools.Diagnostic.Kind.ERROR, message)
    }

    private interface TypeCollection {
        val classStarProjection: Class<*>
    }

    companion object {
        const val KAPT_KOTLIN_GENERATED_OPTION_NAME = "kapt.kotlin.generated"

        private const val TARGET_DIRECTORY_NOT_FOUND =
            "Can't find the target directory for generated Kotlin files."

        private const val CLASS_SUFFIX = "ClassProvider"
        private const val CLASS_PROVIDER_PACKAGE = "com.fabianofranca.annotationprocessing"
        private const val CLASS_PROVIDER_NAME = "ClassProvider"
        private const val CLASS_PROVIDER_PROPERTY_NAME = "classInstance"
    }
}