/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 */

package com.microsoft.azure.toolkit.intellij.common;

import com.microsoft.azure.toolkit.lib.common.form.AzureValidationInfo;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class AzureIntegerInput extends BaseAzureTextInput<Integer> {

    @Setter
    @Getter
    private Integer minValue;
    @Getter
    @Setter
    private Integer maxValue;

    @Nullable
    @Override
    public Integer getValue() {
        final String text = getText();
        if (StringUtils.isBlank(text) || !StringUtils.isNumeric(text)) {
            throw new NumberFormatException(String.format("\"%s\" is not an integer", text));
        }
        return Integer.valueOf(getText());
    }

    @Override
    public void setValue(final Integer val) {
        setText(val == null ? StringUtils.EMPTY : String.valueOf(val));
    }

    @Nonnull
    public AzureValidationInfo doValidate(Integer value) {
        if (value < minValue || value > maxValue) {
            return AzureValidationInfo.error(String.format("Value should be in range [%d, %d]", minValue, maxValue), this);
        } else {
            return AzureValidationInfo.success(this);
        }
    }
}
