package net.nashlegend.compiler;

import com.google.auto.common.SuperficialValidation;
import com.google.auto.service.AutoService;
import com.squareup.javapoet.TypeSpec;

import net.nashlegend.annotations.PrefModel;

import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;

@AutoService(Processor.class)
public class PrefProcessor extends AbstractProcessor {

    String pac = "";

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        Set<? extends Element> PrefElements = roundEnv.getElementsAnnotatedWith(PrefModel.class);
        if (PrefElements.size() == 0) {
            return false;
        }
        for (Element prefElement : PrefElements) {
            if (!SuperficialValidation.validateElement(prefElement)) {
                continue;
            }
        }
        return true;
    }

    private TypeSpec genImlType() {
        return null;
    }
}
