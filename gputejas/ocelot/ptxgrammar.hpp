/* A Bison parser, made by GNU Bison 3.0.2.  */

/* Bison interface for Yacc-like parsers in C

   Copyright (C) 1984, 1989-1990, 2000-2013 Free Software Foundation, Inc.

   This program is free software: you can redistribute it and/or modify
   it under the terms of the GNU General Public License as published by
   the Free Software Foundation, either version 3 of the License, or
   (at your option) any later version.

   This program is distributed in the hope that it will be useful,
   but WITHOUT ANY WARRANTY; without even the implied warranty of
   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
   GNU General Public License for more details.

   You should have received a copy of the GNU General Public License
   along with this program.  If not, see <http://www.gnu.org/licenses/>.  */

/* As a special exception, you may create a larger work that contains
   part or all of the Bison parser skeleton and distribute that work
   under terms of your choice, so long as that work isn't itself a
   parser generator using the skeleton or a modified version thereof
   as a parser skeleton.  Alternatively, if you modify or redistribute
   the parser skeleton itself, you may (at your option) remove this
   special exception, which will cause the skeleton and the resulting
   Bison output files to be licensed under the GNU General Public
   License without this special exception.

   This special exception was added by the Free Software Foundation in
   version 2.2 of Bison.  */

#ifndef YY_YY_RELEASE_BUILD_PTXGRAMMAR_HPP_INCLUDED
# define YY_YY_RELEASE_BUILD_PTXGRAMMAR_HPP_INCLUDED
/* Debug traces.  */
#ifndef YYDEBUG
# define YYDEBUG 0
#endif
#if YYDEBUG
extern int yydebug;
#endif

/* Token type.  */
#ifndef YYTOKENTYPE
# define YYTOKENTYPE
  enum yytokentype
  {
    TOKEN_LABEL = 258,
    TOKEN_IDENTIFIER = 259,
    TOKEN_STRING = 260,
    TOKEN_METADATA = 261,
    TOKEN_INV_PREDICATE_IDENTIFIER = 262,
    TOKEN_PREDICATE_IDENTIFIER = 263,
    OPCODE_COPYSIGN = 264,
    OPCODE_COS = 265,
    OPCODE_SQRT = 266,
    OPCODE_ADD = 267,
    OPCODE_RSQRT = 268,
    OPCODE_MUL = 269,
    OPCODE_SAD = 270,
    OPCODE_SUB = 271,
    OPCODE_EX2 = 272,
    OPCODE_LG2 = 273,
    OPCODE_ADDC = 274,
    OPCODE_RCP = 275,
    OPCODE_SIN = 276,
    OPCODE_REM = 277,
    OPCODE_MUL24 = 278,
    OPCODE_MAD24 = 279,
    OPCODE_DIV = 280,
    OPCODE_ABS = 281,
    OPCODE_NEG = 282,
    OPCODE_MIN = 283,
    OPCODE_MAX = 284,
    OPCODE_MAD = 285,
    OPCODE_MADC = 286,
    OPCODE_SET = 287,
    OPCODE_SETP = 288,
    OPCODE_SELP = 289,
    OPCODE_SLCT = 290,
    OPCODE_MOV = 291,
    OPCODE_ST = 292,
    OPCODE_CVT = 293,
    OPCODE_AND = 294,
    OPCODE_XOR = 295,
    OPCODE_OR = 296,
    OPCODE_CVTA = 297,
    OPCODE_ISSPACEP = 298,
    OPCODE_LDU = 299,
    OPCODE_SULD = 300,
    OPCODE_TXQ = 301,
    OPCODE_SUST = 302,
    OPCODE_SURED = 303,
    OPCODE_SUQ = 304,
    OPCODE_BRA = 305,
    OPCODE_CALL = 306,
    OPCODE_RET = 307,
    OPCODE_EXIT = 308,
    OPCODE_TRAP = 309,
    OPCODE_BRKPT = 310,
    OPCODE_SUBC = 311,
    OPCODE_TEX = 312,
    OPCODE_LD = 313,
    OPCODE_BARSYNC = 314,
    OPCODE_ATOM = 315,
    OPCODE_RED = 316,
    OPCODE_NOT = 317,
    OPCODE_CNOT = 318,
    OPCODE_VOTE = 319,
    OPCODE_SHR = 320,
    OPCODE_SHL = 321,
    OPCODE_FMA = 322,
    OPCODE_MEMBAR = 323,
    OPCODE_PMEVENT = 324,
    OPCODE_POPC = 325,
    OPCODE_PRMT = 326,
    OPCODE_CLZ = 327,
    OPCODE_BFIND = 328,
    OPCODE_BREV = 329,
    OPCODE_BFI = 330,
    OPCODE_BFE = 331,
    OPCODE_TESTP = 332,
    OPCODE_TLD4 = 333,
    OPCODE_BAR = 334,
    OPCODE_PREFETCH = 335,
    OPCODE_PREFETCHU = 336,
    OPCODE_SHFL = 337,
    PREPROCESSOR_INCLUDE = 338,
    PREPROCESSOR_DEFINE = 339,
    PREPROCESSOR_IF = 340,
    PREPROCESSOR_IFDEF = 341,
    PREPROCESSOR_ELSE = 342,
    PREPROCESSOR_ENDIF = 343,
    PREPROCESSOR_LINE = 344,
    PREPROCESSOR_FILE = 345,
    TOKEN_ENTRY = 346,
    TOKEN_EXTERN = 347,
    TOKEN_FILE = 348,
    TOKEN_VISIBLE = 349,
    TOKEN_LOC = 350,
    TOKEN_FUNCTION = 351,
    TOKEN_STRUCT = 352,
    TOKEN_UNION = 353,
    TOKEN_TARGET = 354,
    TOKEN_VERSION = 355,
    TOKEN_SECTION = 356,
    TOKEN_ADDRESS_SIZE = 357,
    TOKEN_WEAK = 358,
    TOKEN_MAXNREG = 359,
    TOKEN_MAXNTID = 360,
    TOKEN_MAXNCTAPERSM = 361,
    TOKEN_MINNCTAPERSM = 362,
    TOKEN_SM11 = 363,
    TOKEN_SM12 = 364,
    TOKEN_SM13 = 365,
    TOKEN_SM20 = 366,
    TOKEN_MAP_F64_TO_F32 = 367,
    TOKEN_SM21 = 368,
    TOKEN_SM10 = 369,
    TOKEN_SM30 = 370,
    TOKEN_SM35 = 371,
    TOKEN_TEXMODE_INDEPENDENT = 372,
    TOKEN_TEXMODE_UNIFIED = 373,
    TOKEN_CONST = 374,
    TOKEN_GLOBAL = 375,
    TOKEN_LOCAL = 376,
    TOKEN_PARAM = 377,
    TOKEN_PRAGMA = 378,
    TOKEN_PTR = 379,
    TOKEN_REG = 380,
    TOKEN_SHARED = 381,
    TOKEN_TEXREF = 382,
    TOKEN_CTA = 383,
    TOKEN_SURFREF = 384,
    TOKEN_GL = 385,
    TOKEN_SYS = 386,
    TOKEN_SAMPLERREF = 387,
    TOKEN_U32 = 388,
    TOKEN_S32 = 389,
    TOKEN_S8 = 390,
    TOKEN_S16 = 391,
    TOKEN_S64 = 392,
    TOKEN_U8 = 393,
    TOKEN_U16 = 394,
    TOKEN_U64 = 395,
    TOKEN_B8 = 396,
    TOKEN_B16 = 397,
    TOKEN_B32 = 398,
    TOKEN_B64 = 399,
    TOKEN_F16 = 400,
    TOKEN_F64 = 401,
    TOKEN_F32 = 402,
    TOKEN_PRED = 403,
    TOKEN_EQ = 404,
    TOKEN_NE = 405,
    TOKEN_LT = 406,
    TOKEN_LE = 407,
    TOKEN_GT = 408,
    TOKEN_GE = 409,
    TOKEN_LS = 410,
    TOKEN_HS = 411,
    TOKEN_EQU = 412,
    TOKEN_NEU = 413,
    TOKEN_LTU = 414,
    TOKEN_LEU = 415,
    TOKEN_GTU = 416,
    TOKEN_GEU = 417,
    TOKEN_NUM = 418,
    TOKEN_NAN = 419,
    TOKEN_HI = 420,
    TOKEN_LO = 421,
    TOKEN_AND = 422,
    TOKEN_OR = 423,
    TOKEN_XOR = 424,
    TOKEN_RN = 425,
    TOKEN_RM = 426,
    TOKEN_RZ = 427,
    TOKEN_RP = 428,
    TOKEN_SAT = 429,
    TOKEN_VOLATILE = 430,
    TOKEN_TAIL = 431,
    TOKEN_UNI = 432,
    TOKEN_ALIGN = 433,
    TOKEN_BYTE = 434,
    TOKEN_WIDE = 435,
    TOKEN_CARRY = 436,
    TOKEN_RNI = 437,
    TOKEN_RMI = 438,
    TOKEN_RZI = 439,
    TOKEN_RPI = 440,
    TOKEN_FTZ = 441,
    TOKEN_APPROX = 442,
    TOKEN_FULL = 443,
    TOKEN_SHIFT_AMOUNT = 444,
    TOKEN_R = 445,
    TOKEN_G = 446,
    TOKEN_B = 447,
    TOKEN_A = 448,
    TOKEN_TO = 449,
    TOKEN_CALL_PROTOTYPE = 450,
    TOKEN_CALL_TARGETS = 451,
    TOKEN_V2 = 452,
    TOKEN_V4 = 453,
    TOKEN_X = 454,
    TOKEN_Y = 455,
    TOKEN_Z = 456,
    TOKEN_W = 457,
    TOKEN_ANY = 458,
    TOKEN_ALL = 459,
    TOKEN_UP = 460,
    TOKEN_DOWN = 461,
    TOKEN_BFLY = 462,
    TOKEN_IDX = 463,
    TOKEN_MIN = 464,
    TOKEN_MAX = 465,
    TOKEN_DEC = 466,
    TOKEN_INC = 467,
    TOKEN_ADD = 468,
    TOKEN_CAS = 469,
    TOKEN_EXCH = 470,
    TOKEN_1D = 471,
    TOKEN_2D = 472,
    TOKEN_3D = 473,
    TOKEN_A1D = 474,
    TOKEN_A2D = 475,
    TOKEN_CUBE = 476,
    TOKEN_ACUBE = 477,
    TOKEN_CA = 478,
    TOKEN_WB = 479,
    TOKEN_CG = 480,
    TOKEN_CS = 481,
    TOKEN_LU = 482,
    TOKEN_CV = 483,
    TOKEN_WT = 484,
    TOKEN_NC = 485,
    TOKEN_L1 = 486,
    TOKEN_L2 = 487,
    TOKEN_P = 488,
    TOKEN_WIDTH = 489,
    TOKEN_DEPTH = 490,
    TOKEN_HEIGHT = 491,
    TOKEN_NORMALIZED_COORDS = 492,
    TOKEN_FILTER_MODE = 493,
    TOKEN_ADDR_MODE_0 = 494,
    TOKEN_ADDR_MODE_1 = 495,
    TOKEN_ADDR_MODE_2 = 496,
    TOKEN_CHANNEL_DATA_TYPE = 497,
    TOKEN_CHANNEL_ORDER = 498,
    TOKEN_TRAP = 499,
    TOKEN_CLAMP = 500,
    TOKEN_ZERO = 501,
    TOKEN_ARRIVE = 502,
    TOKEN_RED = 503,
    TOKEN_POPC = 504,
    TOKEN_SYNC = 505,
    TOKEN_BALLOT = 506,
    TOKEN_F4E = 507,
    TOKEN_B4E = 508,
    TOKEN_RC8 = 509,
    TOKEN_ECL = 510,
    TOKEN_ECR = 511,
    TOKEN_RC16 = 512,
    TOKEN_FINITE = 513,
    TOKEN_INFINITE = 514,
    TOKEN_NUMBER = 515,
    TOKEN_NOT_A_NUMBER = 516,
    TOKEN_NORMAL = 517,
    TOKEN_SUBNORMAL = 518,
    TOKEN_DECIMAL_CONSTANT = 519,
    TOKEN_UNSIGNED_DECIMAL_CONSTANT = 520,
    TOKEN_SINGLE_CONSTANT = 521,
    TOKEN_DOUBLE_CONSTANT = 522
  };
#endif

/* Value type.  */
#if ! defined YYSTYPE && ! defined YYSTYPE_IS_DECLARED
typedef union YYSTYPE YYSTYPE;
union YYSTYPE
{
#line 36 "ocelot/parser/implementation/ptxgrammar.yy" /* yacc.c:1909  */

	char text[1024];
	long long int value;
	long long unsigned int uvalue;

	double doubleFloat;
	float singleFloat;

#line 331 ".release_build/ptxgrammar.hpp" /* yacc.c:1909  */
};
# define YYSTYPE_IS_TRIVIAL 1
# define YYSTYPE_IS_DECLARED 1
#endif

/* Location type.  */
#if ! defined YYLTYPE && ! defined YYLTYPE_IS_DECLARED
typedef struct YYLTYPE YYLTYPE;
struct YYLTYPE
{
  int first_line;
  int first_column;
  int last_line;
  int last_column;
};
# define YYLTYPE_IS_DECLARED 1
# define YYLTYPE_IS_TRIVIAL 1
#endif



//int yyparse (parser::PTXLexer& lexer, parser::PTXParser::State& state);

#endif /* !YY_YY_RELEASE_BUILD_PTXGRAMMAR_HPP_INCLUDED  */
