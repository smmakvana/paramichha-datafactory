package com.paramichha.datafactory.analyzer;

/**
 * The temporal constraint direction resolved from a field's annotations.
 */
public enum TemporalDirection {
    PAST, PAST_OR_NOW, FUTURE, FUTURE_OR_NOW, NONE
}
